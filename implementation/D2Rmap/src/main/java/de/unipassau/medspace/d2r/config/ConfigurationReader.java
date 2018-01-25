package de.unipassau.medspace.d2r.config;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.bridge.DatatypePropertyBridge;
import de.unipassau.medspace.d2r.bridge.ObjectPropertyBridge;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.util.XmlUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Used to read a D2rMap config file.
 */
public class ConfigurationReader {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConfigurationReader.class);

  private RDFProvider provider;

  private RDFFactory rdfFactory;


  /**
   * Constructs a new ConfigurationReader.
   */
  public ConfigurationReader(RDFProvider provider) {
    this.provider = provider;
    this.rdfFactory = provider.getFactory();
  }

  /**
   * Creates a new ConfigurationReader and initializes it with default values specified in {@link D2R}.
   * @return
   */
  public Configuration createDefaultConfig() {
    Configuration config = new Configuration();
    config.getNamespaces().put(D2R.RDFNS_PREFIX, new Namespace(D2R.RDFNS_PREFIX, D2R.RDFNS));

    String format = D2R.Root.OutputFormat.STANDARD_OUTPUT_FORMAT;

    if (!provider.isValid(format)) {
      throw new IllegalStateException("Default output language couldn't be mapped to a Lang object!");
    }

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
  private void readClassMapBridges(Configuration config, Element mapElement) throws D2RException {

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
      ObjectPropertyBridge typeBridge = new ObjectPropertyBridge(rdfFactory, null, maps);
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
  private void readClassMapElement(Configuration config, Element mapElement) throws IOException, D2RException {
    List<D2rMap> maps = config.getMaps();
    D2rMap cMap = new D2rMap(rdfFactory);

    // sql and groupBy attributes are required
    String sqlQuery = mapElement.getAttribute(D2R.ClassMap.SQL_ATTRIBUTE);
    validateSqlQuery(sqlQuery);
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
  private void readConfig(Document document, Configuration config)
      throws IOException, ClassNotFoundException, D2RException {
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
  private void readDataTypePropertyElement(Element elem, D2rMap map) {
    DatatypePropertyBridge bridge = new DatatypePropertyBridge(rdfFactory);
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
  private static void readDBConnectionElement(Configuration config, Element elem)
      throws ClassNotFoundException, D2RException {

    NodeList authentifications = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.DBAuthentification.NAME);
    if (authentifications.getLength() > 0)
      readDBAuthentificationElement(config, (Element)authentifications.item(0));

    // jdbcDSN and jdbcDriver are required attributes
    String jdbcStr = elem.getAttribute(D2R.DBConnection.JDBC_DSN_ATTRIBUTE);
    URI jdbcURI;
    try {
      jdbcURI = new URI(jdbcStr);
    } catch (URISyntaxException e) {
      String errorMessage = "Couldn't get an URI from the jdbc uri: " + jdbcStr;
      throw new D2RException(errorMessage, e);
    }
    config.setJdbc(jdbcURI);

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
   * Reads an ObjectPropertyBridge from a given element for a specific D2rMap.
   * @param elem The element that represents an ObjectPropertyBridge
   * @param map The D2rMap the parsed ObjectPropertyBridge should be added to.
   * @param maps Used to create the new ObjectPropertyBridge.
   * @throws D2RException If the ObjectPropertyBridge couldn't be parsed.
   */
  private void readObjectPropertyElement(Element elem, D2rMap map, List<D2rMap> maps) throws D2RException {

    final String referredClassID = elem.getAttribute(D2R.ObjectPropertyBridge.REFERRED_CLASS_ATTRIBUTE);

    ObjectPropertyBridge bridge = new ObjectPropertyBridge(rdfFactory, referredClassID, maps);
    bridge.setPropertyQName(elem.getAttribute(D2R.ObjectPropertyBridge.PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.ObjectPropertyBridge.PATTERN_ATTRIBUTE));
    bridge.setReferredColumns(elem.getAttribute(D2R.ObjectPropertyBridge.REFERRED_GROUPBY_ATTRIBUTE));
    map.addBridge(bridge);
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
   * @param sqlQuery TODO
   * @throws D2RException
   */
  private static void validateSqlQuery(String sqlQuery) throws D2RException {
    String ucQuery = sqlQuery.toUpperCase();
    if (ucQuery.contains("UNION")) {
        //SelectStatement does not support UNION clauses, yet!
        throw new D2RException("SQL statement should not contain UNION: " + sqlQuery);
    }
  }
}