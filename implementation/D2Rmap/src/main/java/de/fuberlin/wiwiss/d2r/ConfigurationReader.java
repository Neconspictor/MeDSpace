package de.fuberlin.wiwiss.d2r;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medspace.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by David Goeth on 30.05.2017.
 */
public class ConfigurationReader {

  public ConfigurationReader() {

  }

  public static Configuration createDefaultConfig() {
    Configuration config = new Configuration();
    config.getNamespaces().put(D2R.RDFNS_PREFIX, D2R.RDFNS);
    config.setOutputFormat(D2R.STANDARD_OUTPUT_FORMAT);
    config.setSaveAs("StandardOut");
    return config;
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

    list = root.getElementsByTagNameNS(D2R.D2RNS, D2R.CLASS_MAP_ELEMENT);
    for (int i = 0; i < list.getLength(); ++i)
      readClassMapElement(config, (Element)list.item(i));

    // Read translation tables
    list = document.getElementsByTagNameNS(D2R.D2RNS, D2R.TRANSLATION_TABLE_ELEMENT);
    for (int i = 0; i < list.getLength(); ++i)
      readTranslationTableElement(config, (Element)list.item(i));
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

  private static void readTranslationTableElement(Configuration config, Element elem) {
    String tableId = elem.getAttribute(D2R.TRANSLATION_TABLE_ID_ATTRIBUTE).trim();
    TranslationTable table = new TranslationTable();

    // Read Translations
    NodeList list = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.TRANSLATION_ELEMENT);
    for (int i = 0; i < list.getLength(); i++) {
      readTranslationElement(table, (Element)list.item(0));
    }
    config.getTranslationTables().put(tableId, table);
  }

  private static void readTranslationElement(TranslationTable table, Element elem) {
    table.put(elem.getAttribute(D2R.TRANSLATION_KEY_ATTRIBUTE).trim(),
        elem.getAttribute(D2R.TRANSLATION_VALUE_ATTRIBUTE).trim());
  }

  private static void readClassMapElement(Configuration config, Element mapElement) {
      D2RMap cMap = new D2RMap();

      // sql and groupBy attributes are required
      cMap.setSql(mapElement.getAttribute(D2R.CLASS_MAP_SQL_ATTRIBUTE));
      cMap.addResourceIdColumns(mapElement.getAttribute(D2R.CLASS_MAP_RESOURCE_ID_COLUMNS_ATTRIBUTE));

      if (mapElement.hasAttribute(D2R.CLASS_MAP_ID_ATTRIBUTE))
        cMap.setId(mapElement.getAttribute(D2R.CLASS_MAP_ID_ATTRIBUTE));

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

  private static void readDataTypePropertyElement(Element elem, D2RMap map) {
    DatatypePropertyBridge bridge = new DatatypePropertyBridge();
    bridge.setProperty(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setTranslation(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE));
    bridge.setDataType(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_DATA_TYPE_ATTRIBUTE));
    bridge.setXmlLang(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_LANG_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readObjectPropertyElement(Element elem, D2RMap map) {
    ObjectPropertyBridge bridge = new ObjectPropertyBridge();
    bridge.setProperty(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setTranslation(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE));
    bridge.setReferredClass(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_CLASS_ATTRIBUTE));
    bridge.setReferredGroupBy(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_GROUPBY_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readOutputFormatElement(Configuration config, Element elem) {
      config.setOutputFormat(elem.getTextContent());
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

  private static void readDBAuthentificationElement(Configuration config, Element elem) {
    config.setDatabaseUsername(elem.getAttribute(D2R.DBAUTHENTIFICATION_USERNAME_ATTRIBUTE));
    String password = elem.getAttribute(D2R.DBAUTHENTIFICATION_PASSWORD_ATTRIBUTE);
    if (password == null) password = "";
    config.setDatabasePassword(password);
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

    config.getNamespaces().put(elem.getAttribute(D2R.NAMESPACE_PREFIX_ATTRIBUTE),
        elem.getAttribute(D2R.NAMESPACE_NAMESPACE_ATTRIBUTE));
  }
}