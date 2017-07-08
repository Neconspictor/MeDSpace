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
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Created by David Goeth on 30.05.2017.
 */
public class ConfigurationReader {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(ConfigurationReader.class);

  public ConfigurationReader() {

  }

  public static Configuration createDefaultConfig() throws D2RException {
    Configuration config = new Configuration();
    config.getNamespaces().put(D2R.RDFNS_PREFIX, new Namespace(D2R.RDFNS_PREFIX, D2R.RDFNS));

    config.setOutputFormat(getLangFromString(D2R.STANDARD_OUTPUT_FORMAT));
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
   */
  public Configuration readConfig(String filename) throws IOException, D2RException {
    Configuration config = createDefaultConfig();
    try {
      // Read document into DOM
      Schema schema = XmlUtil.createSchema(new String[]{D2R.MEDSPACE_VALIDATION_SCHEMA});
      Document document = XmlUtil.parseFromFile(filename, schema);

      //read the Document
      readConfig(document, config);
    }
    catch (SAXParseException spe) {
      throw new D2RException("Error while parsing XML file: " + "line " +
          spe.getLineNumber() +
          ", uri: " + spe.getSystemId() + ", reason: " +
          spe.getMessage(), spe);

    } catch (SAXException sxe) {
      throw new D2RException("Error while parsing XML file: ", sxe);

    } catch (IOException e) {
      throw new D2RException("IO Error while parsing the map: ", e);
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

  private static void readClassMapElement(Configuration config, Element mapElement) throws D2RException {
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

    // Read type attribute
    if (mapElement.hasAttribute(D2R.CLASS_MAP_TYPE_ATTRIBUTE)) {
      // add rdf:type bridge
      String value = mapElement.getAttribute(D2R.CLASS_MAP_TYPE_ATTRIBUTE);
      ObjectPropertyBridge typeBridge = new ObjectPropertyBridge();
      typeBridge.setProperty("rdf:type");
      typeBridge.setPattern(value);
      cMap.addBridge(typeBridge);
    }

    // Read uriPattern
    if (mapElement.hasAttribute(D2R.CLASS_MAP_BASE_URI_ATTRIBUTE))
      cMap.setBaseURI(mapElement.getAttribute(D2R.CLASS_MAP_BASE_URI_ATTRIBUTE));

    // Read datatype property mappings
    NodeList propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.DATA_TYPE_PROPERTY_BRIDGE_ELEMENT);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readDataTypePropertyElement((Element)propertyList.item(i), cMap);

    // Read object property mappings
    propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.OBJECT_PROPERTY_BRIDGE_ELEMENT);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readObjectPropertyElement((Element)propertyList.item(i), cMap);

    config.getMaps().add(cMap);
  }

  private static void readConfig(Document document, Configuration config) throws IOException, D2RException {
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
      throw new D2RException("No root element was specified in the mapping");

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
  }

  private static void readDataTypePropertyElement(Element elem, D2rMap map) {
    DatatypePropertyBridge bridge = new DatatypePropertyBridge();
    bridge.setProperty(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setDataType(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_DATA_TYPE_ATTRIBUTE));
    bridge.setXmlLang(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_LANG_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readDBAuthentificationElement(Configuration config, Element elem) {
    config.setDatabaseUsername(elem.getAttribute(D2R.DBAUTHENTIFICATION_USERNAME_ATTRIBUTE));
    String password = elem.getAttribute(D2R.DBAUTHENTIFICATION_PASSWORD_ATTRIBUTE);
    if (password == null) password = "";
    config.setDatabasePassword(password);
  }

  private static void readDBConnectionElement(Configuration config, Element elem) {
    NodeList authentifications = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.DBAUTHENTIFICATION_ELEMENT);
    if (authentifications.getLength() > 0)
      readDBAuthentificationElement(config, (Element)authentifications.item(0));

    // jdbcDSN and jdbcDriver are required attributes
    config.setJdbc(elem.getAttribute(D2R.DBCONNECTION_JDBC_DSN_ATTRIBUTE));
    config.setJdbcDriver(elem.getAttribute(D2R.DBCONNECTION_JDBC_DRIVER_ATTRIBUTE));

    int maxConnections = 5;
    if (elem.hasAttribute(D2R.DBCONNECTION_MAX_CONNECTIONS_ATTRIBUTE)) {
      maxConnections = Integer.parseInt(elem.getAttribute(D2R.DBCONNECTION_MAX_CONNECTIONS_ATTRIBUTE));
    }

    config.setMaxConnections(maxConnections);

    //read datasource config properties being vendor specific
    parseDataSourceSpecificProperties(elem, config);
  }

  private static void readIndexElement(Configuration config, Element elem) throws D2RException {
    String directory = elem.getAttribute(D2R.INDEX_DIRECTORY_ATTRIBUTE);

    if (directory == null) {
      throw new D2RException("No index directoyr specified!");
    }

    Path path = null;

    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      log.error(e);
      throw new D2RException("Couldn't create index directory!");
    }


    config.setUseIndex(true);
    config.setIndexDirectory(path);
  }

  private static void readObjectPropertyElement(Element elem, D2rMap map) {
    ObjectPropertyBridge bridge = new ObjectPropertyBridge();
    bridge.setProperty(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setReferredClassID(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_CLASS_ATTRIBUTE));
    bridge.setReferredGroupBy(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_GROUPBY_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readOutputFormatElement(Configuration config, Element elem) throws D2RException {
    String format = elem.getTextContent();
    Lang lang = null;

    assert format != null;

    try {
      lang = getLangFromString(format);
    } catch (D2RException e) {
      throw new D2RException("Unknown output format: " + format);
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