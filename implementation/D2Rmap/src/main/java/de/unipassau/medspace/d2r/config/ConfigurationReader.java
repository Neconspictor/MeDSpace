package de.unipassau.medspace.d2r.config;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.bridge.DatatypePropertyBridge;
import de.unipassau.medspace.d2r.bridge.ObjectPropertyBridge;
import de.unipassau.medspace.d2r.exception.D2RException;
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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to read a D2rMap config file.
 */
public class ConfigurationReader {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConfigurationReader.class);

  /**
   * Contains all supported org.apache.jena.riot.Lang objects that are supported by the jena framework
   * to be used for streaming. Not all rdf serialization formats supports to stream the triple result set,
   * so not all jena rdf languages are supported.
   */
  private Set<Lang> supportedStreamLanguages;

  /**
   * Constructs a new ConfigurationReader.
   */
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

  /**
   * Creates a new ConfigurationReader and initializes it with default values specified in {@link D2R}.
   * @return
   */
  public static Configuration createDefaultConfig() {
    Configuration config = new Configuration();
    config.getNamespaces().put(D2R.RDFNS_PREFIX, new Namespace(D2R.RDFNS_PREFIX, D2R.RDFNS));

    Lang lang = null;

    try {
      lang = getLangFromString(D2R.Root.OutputFormat.STANDARD_OUTPUT_FORMAT);
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
    String prefix = elem.getAttribute(D2R.Namespace.PREFIX_ATTRIBUTE);
    String namespace = elem.getAttribute(D2R.Namespace.NAMESPACE_ATTRIBUTE);

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

  /**
   * Parses a string that represents an jena rdf language to the representing java object.
   * @param format The string that represent a jena rdf language.
   * @return The rdf language.
   * @throws D2RException If the string couldn't be parsed.
   */
  private static Lang getLangFromString(String format) throws D2RException {
    assert format != null;

    Lang lang = RDFLanguages.shortnameToLang(format);

    if (lang == null)
      throw new D2RException("Unknown language format: " + format);

    return lang;
  }

  /**
   * Parses a list of datasource properties from a given D2rMap root element and adds it to the specified Configuration.
   * @param root The root element of a valid D2rMap config.
   * @param config The configuration the read datasource properties should be added to.
   */
  private static void parseDataSourceSpecificProperties(Element root, Configuration config) {
    NodeList list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.DataSourceProperty.NAME);

    for (int i = 0; i < list.getLength(); ++i) {
      Element elem = (Element)list.item(i);
      String propertyName = elem.getAttribute(D2R.DataSourceProperty.NAME_ATTRIBUTE);
      String value = elem.getAttribute(D2R.DataSourceProperty.VALUE_ATTRIBUTE);
      config.addDataSourceProperty(propertyName, value);
    }
  }

  /**
   * Reads all D2r bridges specified in a specific ClassMap element.
   * @param config Used to get the list of all read D2rMaps.
   * @param mapElement The ClassMap element.
   * @throws D2RException If a parse error occurs.
   */
  private static void readClassMapBridges(Configuration config, Element mapElement) throws D2RException {

    String id = mapElement.getAttribute(D2R.ClassMap.ID_ATTRIBUTE);
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
    if (mapElement.hasAttribute(D2R.ClassMap.TYPE_ATTRIBUTE)) {
      // add rdf:type bridge
      String value = mapElement.getAttribute(D2R.ClassMap.TYPE_ATTRIBUTE);
      ObjectPropertyBridge typeBridge = new ObjectPropertyBridge(null, maps);
      typeBridge.setPropertyQName("rdf:type");
      typeBridge.setPattern(value);
      map.addBridge(typeBridge);
    }

    // Read datatype propertyQName mappings
    NodeList propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.DataTypePropertyBridge.NAME);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readDataTypePropertyElement((Element)propertyList.item(i), map);

    // Read object propertyQName mappings
    propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.ObjectPropertyBridge.NAME);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readObjectPropertyElement((Element)propertyList.item(i), map, maps);
  }

  /**
   * Reads a ClassMap element, creates a D2rMap from it and adds it to the specified Configuration.
   * @param config The Configuration the created D2rMap should be added to.
   * @param mapElement The element that represents a ClassMap element.
   * @throws IOException If an IO-Error occurs.
   * @throws D2RException If a parse error occurs.
   */
  private static void readClassMapElement(Configuration config, Element mapElement) throws IOException, D2RException {
    List<D2rMap> maps = config.getMaps();
    D2rMap cMap = new D2rMap();

    // sql and groupBy attributes are required
    String sqlQuery = mapElement.getAttribute(D2R.ClassMap.SQL_ATTRIBUTE);
    validateSqlQuery(sqlQuery, config.isIndexUsed());
    cMap.setSql(sqlQuery);
    cMap.addResourceIdColumns(mapElement.getAttribute(D2R.ClassMap.RESOURCE_ID_COLUMNS_ATTRIBUTE));

    String id = mapElement.getAttribute(D2R.ClassMap.ID_ATTRIBUTE);
    validateD2RMapId(id, maps);

    cMap.setId(id);

    // Read uriPattern
    if (mapElement.hasAttribute(D2R.ClassMap.BASE_URI_ATTRIBUTE))
      cMap.setBaseURI(mapElement.getAttribute(D2R.ClassMap.BASE_URI_ATTRIBUTE));


    config.getMaps().add(cMap);
  }

  /**
   * Parses a D2rMap config from a given XML document and stores its content in the specified Configuration.
   * @param document The xml document that represents a valid D2rMap config file.
   * @param config The Configuration to fill.
   * @throws IOException If the document does not contain a D2rMap root element or another IO-Error occurs.
   * @throws ClassNotFoundException If the jdbc driver class, specified in the D2rMap config, couldn't be found in the
   * class path.
   * @throws D2RException if another parse error occurs.
   */
  private void readConfig(Document document, Configuration config) throws IOException,
                                                                                 ClassNotFoundException,
                                                                                 D2RException {
    // Read namespaces
    NodeList list = document.getElementsByTagNameNS(D2R.D2RNS, D2R.Namespace.NAME);
    int numNodes = list.getLength();
    for (int i = 0; i < numNodes; i++) {
      Element elem = (Element) list.item(i);
      readComplexTypeNamespace(config, elem);
    }

    // Read database connection
    list = document.getElementsByTagNameNS(D2R.D2RNS, D2R.Root.NAME);
    Element root = (Element) list.item(0);

    if (root == null)
      throw new IOException("No root element was specified in the mapping");

    // DBConnection is a required element that exists exact one time
    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.DBConnection.NAME);
    readDBConnectionElement(config, (Element)list.item(0));
    
    //OutputFormat is a required element that exists exact one time
    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.Root.OutputFormat.NAME);
    readOutputFormatElement(config, (Element) list.item(0));

    // check if a index is wished and if it is the case, then read ut the index store directory
    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.Index.NAME);
    if (list.getLength() > 0)
    readIndexElement(config, (Element) list.item(0));

    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.ClassMap.NAME);
    for (int i = 0; i < list.getLength(); ++i)
      readClassMapElement(config, (Element)list.item(i));

    // after reading all D2rMaps we can read the bridges
    for (int i = 0; i < list.getLength(); ++i)
      readClassMapBridges(config, (Element)list.item(i));
  }

  /**
   * Parses a DataTypePropertyBridge from a given element and adds it to the given D2rMap.
   * @param elem Represents a DataTypePropertyBridge element
   * @param map The D2rMap the read DataTypePropertyBridge should be added to.
   */
  private static void readDataTypePropertyElement(Element elem, D2rMap map) {
    DatatypePropertyBridge bridge = new DatatypePropertyBridge();
    bridge.setPropertyQName(elem.getAttribute(D2R.DataTypePropertyBridge.PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.DataTypePropertyBridge.PATTERN_ATTRIBUTE));
    bridge.setDataType(elem.getAttribute(D2R.DataTypePropertyBridge.DATA_TYPE_ATTRIBUTE));
    bridge.setLangTag(elem.getAttribute(D2R.DataTypePropertyBridge.LANG_ATTRIBUTE));
    map.addBridge(bridge);
  }

  /**
   * Parses a DBAuthentification element and updates the specified Configuration.
   * @param config The configuration that should be updated.
   * @param elem Represents a DBAuthentification element.
   */
  private static void readDBAuthentificationElement(Configuration config, Element elem) {
    config.setDatabaseUsername(elem.getAttribute(D2R.DBAuthentification.USERNAME_ATTRIBUTE));
    String password = elem.getAttribute(D2R.DBAuthentification.PASSWORD_ATTRIBUTE);
    if (password == null) password = "";
    config.setDatabasePassword(password);
  }

  /**
   * Parses a DBConnection element and updates the specified Configuration.
   * @param config The configuration that should be updated.
   * @param elem Represents a DBConnection element.
   * @throws ClassNotFoundException thrown if the jdbcDriver attribute couldn't be matched to a valid jdbc driver class.
   */
  private static void readDBConnectionElement(Configuration config, Element elem) throws ClassNotFoundException {
    NodeList authentifications = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.DBAuthentification.NAME);
    if (authentifications.getLength() > 0)
      readDBAuthentificationElement(config, (Element)authentifications.item(0));

    // jdbcDSN and jdbcDriver are required attributes
    config.setJdbc(elem.getAttribute(D2R.DBConnection.JDBC_DSN_ATTRIBUTE));
    Class driver = Class.forName(elem.getAttribute(D2R.DBConnection.JDBC_DRIVER_ATTRIBUTE));
    config.setJdbcDriver(driver);

    int maxConnections = 5;
    if (elem.hasAttribute(D2R.DBConnection.MAX_CONNECTIONS_ATTRIBUTE)) {
      maxConnections = Integer.parseInt(elem.getAttribute(D2R.DBConnection.MAX_CONNECTIONS_ATTRIBUTE));
    }

    config.setMaxConnections(maxConnections);

    //read datasource config properties being vendor specific
    parseDataSourceSpecificProperties(elem, config);
  }

  /**
   * Reads the index directory from a given element and adds it to the given Configuration.
   * @param config The configuration the read index directory should be added to.
   * @param elem The element that represents an index element.
   * @throws IOException If the index directory couldn't be parsed.
   */
  private static void readIndexElement(Configuration config, Element elem) throws IOException {
    String directory = elem.getAttribute(D2R.Index.DIRECTORY_ATTRIBUTE);

    if (directory == null) {
      throw new IllegalArgumentException("index directory doesn't be null!");
    }

    Path path = null;

    try {
      //path = FileUtil.createDirectory(directory);
      path = Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IOException("Error while trying to createDoc index directory path", e);
    }


    config.setUseIndex(true);
    config.setIndexDirectory(path);
  }

  /**
   * Reads an ObjectPropertyBridge from a given element for a specific D2rMap.
   * @param elem The element that represents an ObjectPropertyBridge
   * @param map The D2rMap the parsed ObjectPropertyBridge should be added to.
   * @param maps Used to create the new ObjectPropertyBridge.
   * @throws D2RException If the ObjectPropertyBridge couldn't be parsed.
   */
  private static void readObjectPropertyElement(Element elem, D2rMap map, List<D2rMap> maps) throws D2RException {

    final String referredClassID = elem.getAttribute(D2R.ObjectPropertyBridge.REFERRED_CLASS_ATTRIBUTE);

    ObjectPropertyBridge bridge = new ObjectPropertyBridge(referredClassID, maps);
    bridge.setPropertyQName(elem.getAttribute(D2R.ObjectPropertyBridge.PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.ObjectPropertyBridge.PATTERN_ATTRIBUTE));
    bridge.setReferredColumns(elem.getAttribute(D2R.ObjectPropertyBridge.REFERRED_GROUPBY_ATTRIBUTE));
    map.addBridge(bridge);
  }

  /**
   * Parses the rdf output format language
   * @param config The configuration the readed output format should be added to.
   * @param elem The element that contains the output format.
   * @throws D2RException If the rdf language couldn't be parsed from the content of the specified element.
   */
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

  /**
   * Tests whether a given sql query is valid for a sql attribute from a ClassMap element.
   * @param sqlQuery
   * @param indexUsed
   * @throws D2RException
   */
  private static void validateSqlQuery(String sqlQuery, boolean indexUsed) throws D2RException {
    String ucQuery = sqlQuery.toUpperCase();
    if (ucQuery.contains("UNION")) {
      if (!indexUsed) {
        //SelectStatement does not support UNION clauses, yet!
        throw new D2RException("SQL statement should not contain UNION: " + sqlQuery);
      }
    }
  }
}