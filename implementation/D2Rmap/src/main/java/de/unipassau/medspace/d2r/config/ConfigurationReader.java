package de.unipassau.medspace.d2r.config;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.bridge.DatatypePropertyBridge;
import de.unipassau.medspace.d2r.bridge.ObjectPropertyBridge;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.XmlUtil;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by David Goeth on 30.05.2017.
 */
public class ConfigurationReader {

  /** log4j logger used for this class */
  private static Logger log = LoggerFactory.getLogger(ConfigurationReader.class);

  /**
   * Contains all supported org.apache.jena.riot.Lang objects that are supported by the jena framework
   * to be used for streaming. Not all rdf serialization formats supports to stream the triple result set,
   * so not all jena rdf languages are supported.
   */
  private Set<Lang> supportedStreamLanguages;

  public ConfigurationReader() {
    supportedStreamLanguages = new HashSet<>();
    Collection<RDFFormat> formats = StreamRDFWriter.registered();
    for (RDFFormat format : formats) {
      supportedStreamLanguages.add(format.getLang());
    }

    // delete rdf/null, as it only outputs an empty rdf graph
    // -> not very useful for exporting data.
    supportedStreamLanguages.remove(Lang.RDFNULL);
  }

  public static Configuration createDefaultConfig() {
    Configuration config = new Configuration();
    config.getNamespaces().put(D2R.RDFNS_PREFIX, new Namespace(D2R.RDFNS_PREFIX, D2R.RDFNS));

    Lang lang = null;

    try {
      lang = getLangFromString(D2R.STANDARD_OUTPUT_FORMAT);
    } catch (D2RException e) {
      throw new IllegalStateException("Default output language couldn't be mapped to a Lang object!");
    }

    config.setOutputFormat(lang);
    return config;
  }

  /**
   * Reads a namespace element and adds it to the provided Configuration.
   * @param config the Configuration the namespace element should be added to
   * @param elem The namesapce element
   */
  public  static void readComplexTypeNamespace(Configuration config, Element elem) {
    String prefix = elem.getAttribute(D2R.NAMESPACE_PREFIX_ATTRIBUTE);
    String namespace = elem.getAttribute(D2R.NAMESPACE_NAMESPACE_ATTRIBUTE);

    if (prefix.equals(""))
      throw new IllegalStateException("prefix not set or empty.");
    if (namespace.equals(""))
      throw new IllegalStateException("namespace not set or empty.");

    config.getNamespaces().put(prefix, new Namespace(prefix, namespace));
  }

  /**
   * Reads an D2R Map from the filesystem.
   * @param filename of the D2R Map
   * @return The configuration file
   * @throws IOException if an error occurs
   */
  public Configuration readConfig(String filename) throws IOException {
    Configuration config = createDefaultConfig();
    try {
      // Read document into DOM
      Schema schema = XmlUtil.createSchema(new String[]{D2R.MEDSPACE_VALIDATION_SCHEMA});
      Document document = XmlUtil.parseFromFile(filename, schema);

      //read the Document
      readConfig(document, config);
    }
    catch (SAXParseException spe) {
      throw new IOException("Error while parsing XML file: " + "line " +
          spe.getLineNumber() +
          ", uri: " + spe.getSystemId() + ", reason: " +
          spe.getMessage(), spe);

    } catch (ClassNotFoundException  | D2RException | IOException | SAXException e) {
      throw new IOException("Error while parsing XML file: ", e);
    }

    return config;
  }

  private static Lang getLangFromString(String format) throws D2RException {
    assert format != null;

    Lang lang = RDFLanguages.shortnameToLang(format);

    if (lang == null)
      throw new D2RException("Unknown language format: " + format);

    return lang;
  }

  private static void parseDataSourceSpecificProperties(Element root, Configuration config) {
    NodeList list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.DATA_SOURCE_PROPERTY_ELEMENT);

    for (int i = 0; i < list.getLength(); ++i) {
      Element elem = (Element)list.item(i);
      String propertyName = elem.getAttribute(D2R.DATA_SOURCE_PROPERTY_NAME_ATTRIBUTE);
      String value = elem.getAttribute(D2R.DATA_SOURCE_PROPERTY_VALUE_ATTRIBUTE);
      config.addDataSourceProperty(propertyName, value);
    }
  }

  private static void readClassMapBridges(Configuration config, Element mapElement) throws D2RException {

    String id = mapElement.getAttribute(D2R.CLASS_MAP_ID_ATTRIBUTE);
    id = id.trim().toUpperCase();
    final List<D2rMap> maps = config.getMaps();
    D2rMap map = null;

    for (D2rMap m : maps) {
      if (m.getId().equals(id)) {
        map = m;
        break;
      }
    }

    if (map == null) throw new D2RException("Couldn't find D2rMap with id = " + id);


    // Read type attribute
    if (mapElement.hasAttribute(D2R.CLASS_MAP_TYPE_ATTRIBUTE)) {
      // add rdf:type bridge
      String value = mapElement.getAttribute(D2R.CLASS_MAP_TYPE_ATTRIBUTE);
      ObjectPropertyBridge typeBridge = new ObjectPropertyBridge(null, maps);
      typeBridge.setPropertyQName("rdf:type");
      typeBridge.setPattern(value);
      map.addBridge(typeBridge);
    }

    // Read datatype propertyQName mappings
    NodeList propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.DATA_TYPE_PROPERTY_BRIDGE_ELEMENT);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readDataTypePropertyElement((Element)propertyList.item(i), map);

    // Read object propertyQName mappings
    propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.OBJECT_PROPERTY_BRIDGE_ELEMENT);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readObjectPropertyElement((Element)propertyList.item(i), map, maps);
  }

  private static void readClassMapElement(Configuration config, Element mapElement) throws IOException, D2RException {
    List<D2rMap> maps = config.getMaps();
    D2rMap cMap = new D2rMap();

    // sql and groupBy attributes are required
    String sqlQuery = mapElement.getAttribute(D2R.CLASS_MAP_SQL_ATTRIBUTE);
    validateSqlQuery(sqlQuery);
    cMap.setSql(sqlQuery);
    cMap.addResourceIdColumns(mapElement.getAttribute(D2R.CLASS_MAP_RESOURCE_ID_COLUMNS_ATTRIBUTE));

    String id = mapElement.getAttribute(D2R.CLASS_MAP_ID_ATTRIBUTE);
    validateD2RMapId(id, maps);

    cMap.setId(id);

    // Read uriPattern
    if (mapElement.hasAttribute(D2R.CLASS_MAP_BASE_URI_ATTRIBUTE))
      cMap.setBaseURI(mapElement.getAttribute(D2R.CLASS_MAP_BASE_URI_ATTRIBUTE));


    config.getMaps().add(cMap);
  }

  private void readConfig(Document document, Configuration config) throws IOException,
                                                                                 ClassNotFoundException,
                                                                                 D2RException {
    // Read namespaces
    NodeList list = document.getElementsByTagNameNS(D2R.D2RNS, D2R.NAMESPACE_ELEMENT);
    int numNodes = list.getLength();
    for (int i = 0; i < numNodes; i++) {
      Element elem = (Element) list.item(i);
      readComplexTypeNamespace(config, elem);
    }

    // Read database connection
    list = document.getElementsByTagNameNS(D2R.D2RNS, D2R.ROOT_ELEMENT);
    Element root = (Element) list.item(0);

    if (root == null)
      throw new IOException("No root element was specified in the mapping");

    // DBConnection is a required element that exists exact one time
    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.DBCONNECTION_ELEMENT);
    readDBConnectionElement(config, (Element)list.item(0));
    
    //OutputFormat is a required element that exists exact one time
    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.OUTPUT_FORMAT_ELEMENT);
    readOutputFormatElement(config, (Element) list.item(0));

    // check if a index is wished and if it is the case, then read ut the index store directory
    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.INDEX_ELEMENT);
    if (list.getLength() > 0)
    readIndexElement(config, (Element) list.item(0));

    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.CLASS_MAP_ELEMENT);
    for (int i = 0; i < list.getLength(); ++i)
      readClassMapElement(config, (Element)list.item(i));

    // after reading all D2rMaps we can read the bridges
    for (int i = 0; i < list.getLength(); ++i)
      readClassMapBridges(config, (Element)list.item(i));
  }

  private static void readDataTypePropertyElement(Element elem, D2rMap map) {
    DatatypePropertyBridge bridge = new DatatypePropertyBridge();
    bridge.setPropertyQName(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setDataType(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_DATA_TYPE_ATTRIBUTE));
    bridge.setLangTag(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_LANG_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readDBAuthentificationElement(Configuration config, Element elem) {
    config.setDatabaseUsername(elem.getAttribute(D2R.DBAUTHENTIFICATION_USERNAME_ATTRIBUTE));
    String password = elem.getAttribute(D2R.DBAUTHENTIFICATION_PASSWORD_ATTRIBUTE);
    if (password == null) password = "";
    config.setDatabasePassword(password);
  }

  private static void readDBConnectionElement(Configuration config, Element elem) throws ClassNotFoundException {
    NodeList authentifications = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.DBAUTHENTIFICATION_ELEMENT);
    if (authentifications.getLength() > 0)
      readDBAuthentificationElement(config, (Element)authentifications.item(0));

    // jdbcDSN and jdbcDriver are required attributes
    config.setJdbc(elem.getAttribute(D2R.DBCONNECTION_JDBC_DSN_ATTRIBUTE));
    Class driver = Class.forName(elem.getAttribute(D2R.DBCONNECTION_JDBC_DRIVER_ATTRIBUTE));
    config.setJdbcDriver(driver);

    int maxConnections = 5;
    if (elem.hasAttribute(D2R.DBCONNECTION_MAX_CONNECTIONS_ATTRIBUTE)) {
      maxConnections = Integer.parseInt(elem.getAttribute(D2R.DBCONNECTION_MAX_CONNECTIONS_ATTRIBUTE));
    }

    config.setMaxConnections(maxConnections);

    //read datasource config properties being vendor specific
    parseDataSourceSpecificProperties(elem, config);
  }

  private static void readIndexElement(Configuration config, Element elem) throws IOException {
    String directory = elem.getAttribute(D2R.INDEX_DIRECTORY_ATTRIBUTE);

    if (directory == null) {
      throw new IllegalArgumentException("index directory doesn't be null!");
    }

    Path path = null;

    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Error while trying to createDoc index directory path", e);
    }


    config.setUseIndex(true);
    config.setIndexDirectory(path);
  }

  private static void readObjectPropertyElement(Element elem, D2rMap map, List<D2rMap> maps) throws D2RException {

    final String referredClassID = elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_CLASS_ATTRIBUTE);

    ObjectPropertyBridge bridge = new ObjectPropertyBridge(referredClassID, maps);
    bridge.setPropertyQName(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setReferredColumns(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_GROUPBY_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private void readOutputFormatElement(Configuration config, Element elem) throws D2RException {
    String format = elem.getTextContent();
    Lang lang = null;

    assert format != null;

    try {
      lang = getLangFromString(format);
    } catch (D2RException e) {
      throw new D2RException("Error while retrieving rdf language", e);
    }

    if (!supportedStreamLanguages.contains(lang)) {
      StringBuilder supportedLangs = new StringBuilder();
      for (Lang l : supportedStreamLanguages) {
        supportedLangs.append(l.getLabel());
        supportedLangs.append("\n");
      }

      supportedLangs.delete(supportedLangs.length() -1, supportedLangs.length());

      throw new D2RException("RDF language isn't supported for streaming rdf triples: " + lang.getLabel() +
      "\nSupported languages are:\n" + supportedLangs.toString());
    }

    config.setOutputFormat(lang);
  }

  /**
   * Validates the value of a DRMap id. If the validation test should fail, a D2RException is thrown
   * @param id the D2rMap id
   * @param maps a collection of D2RMaps that have been read so far
   * @throws D2RException if the validation test fails
   */
  private static void validateD2RMapId(String id, Collection<D2rMap> maps) throws D2RException {

    for (D2rMap map : maps) {
      if (map.getId().equals(id)) {
        throw new D2RException("D2rMap id is multiple times used in the configuration file: id=" + id);
      }
    }
  }

  private static void validateSqlQuery(String sqlQuery) throws D2RException {
    String ucQuery = sqlQuery.toUpperCase();
    if (ucQuery.contains("UNION"))
      throw new D2RException("SQL statement should not contain UNION: " + sqlQuery);
  }
}