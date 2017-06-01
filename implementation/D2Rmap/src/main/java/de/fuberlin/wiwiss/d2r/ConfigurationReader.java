package de.fuberlin.wiwiss.d2r;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by David Goeth on 30.05.2017.
 */
public class ConfigurationReader {

  public static Configuration createDefaultConfig() {
    Configuration config = new Configuration();
    config.setMaps(new Vector<>());
    config.setNamespaces(new HashMap<>());
    config.getNamespaces().put(D2R.RDFNS_PREFIX, D2R.RDFNS);
    config.setTranslationTables(new HashMap<>());
    config.setOutputFormat(D2R.STANDARD_OUTPUT_FORMAT);
    config.setSaveAs("StandardOut");
    return config;
  }

  public static void readConfig(Document document, Configuration config) throws IOException, D2RException {
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

  private static void readTranslationTableElement(Configuration config, Element elem) {
    String tableId = elem.getAttribute(D2R.TRANSLATION_TABLE_ID_ATTRIBUTE).trim();
    TranslationTable table = new TranslationTable();

    // Read Translations
    NodeList list = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.TRANSLATION_ELEMENT);
    for (int i = 0; i < list.getLength(); i++) {
      readTranslationElement(table, (Element)list.item(0));
    }
    config.translationTables.put(tableId, table);
  }

  private static void readTranslationElement(TranslationTable table, Element elem) {
    table.put(elem.getAttribute(D2R.TRANSLATION_KEY_ATTRIBUTE).trim(),
        elem.getAttribute(D2R.TRANSLATION_VALUE_ATTRIBUTE).trim());
  }

  private static void readClassMapElement(Configuration config, Element mapElement) {
      D2RMap cMap = new D2RMap();

      // sql and groupBy attributes are required
      cMap.setSql(mapElement.getAttribute(D2R.CLASS_MAP_SQL_ATTRIBUTE));
      cMap.addGroupByFields(mapElement.getAttribute(D2R.CLASS_MAP_GROUPBY_ATTRIBUTE));

      if (mapElement.hasAttribute(D2R.CLASS_MAP_ID_ATTRIBUTE))
        cMap.setId(mapElement.getAttribute(D2R.CLASS_MAP_ID_ATTRIBUTE));

      // Read type attribute
      if (mapElement.hasAttribute(D2R.CLASS_MAP_TYPE_ATTRIBUTE)) {
        // add rdf:type bridge
        String value = mapElement.getAttribute(D2R.CLASS_MAP_TYPE_ATTRIBUTE);
        ObjectPropertyBridge typeBridge = new ObjectPropertyBridge();
        typeBridge.setProperty("rdf:type");
        typeBridge.setValue(value);
        cMap.addBridge(typeBridge);
      }

      // Read uriPattern
      if (mapElement.hasAttribute(D2R.CLASS_MAP_URI_PATTERN_ATTRIBUTE))
        cMap.setUriPattern(mapElement.getAttribute(D2R.CLASS_MAP_URI_PATTERN_ATTRIBUTE));
    // Read uriCoulmn
      if (mapElement.hasAttribute(D2R.CLASS_MAP_URI_COLUMN_ATTRIBUTE))
        cMap.setUriColumn(mapElement.getAttribute(D2R.CLASS_MAP_URI_COLUMN_ATTRIBUTE));

    // Read datatype property mappings
    NodeList propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.DATA_TYPE_PROPERTY_BRIDGE_ELEMENT);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readDataTypePropertyElement((Element)propertyList.item(i), cMap);

    // Read object property mappings
    propertyList = mapElement.getElementsByTagNameNS(D2R.D2RNS, D2R.OBJECT_PROPERTY_BRIDGE_ELEMENT);
    for (int i = 0; i< propertyList.getLength(); ++i)
      readObjectPropertyElement((Element)propertyList.item(i), cMap);

      config.maps.add(cMap);
  }

  private static void readDataTypePropertyElement(Element elem, D2RMap map) {
    DatatypePropertyBridge bridge = new DatatypePropertyBridge();
    bridge.setProperty(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setColumn(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_COLUMN_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setValue(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_VALUE_ATTRIBUTE));
    bridge.setTranslation(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE));
    bridge.setDataType(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_DATA_TYPE_ATTRIBUTE));
    bridge.setXmlLang(elem.getAttribute(D2R.DATA_TYPE_PROPERTY_BRIDGE_LANG_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readObjectPropertyElement(Element elem, D2RMap map) {
    ObjectPropertyBridge bridge = new ObjectPropertyBridge();
    bridge.setProperty(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE));
    bridge.setColumn(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_COLUMN_ATTRIBUTE));
    bridge.setPattern(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE));
    bridge.setValue(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_VALUE_ATTRIBUTE));
    bridge.setTranslation(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE));
    bridge.setReferredClass(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_CLASS_ATTRIBUTE));
    bridge.setReferredGroupBy(elem.getAttribute(D2R.OBJECT_PROPERTY_BRIDGE_REFERRED_GROUPBY_ATTRIBUTE));
    map.addBridge(bridge);
  }

  private static void readOutputFormatElement(Configuration config, Element elem) {
      config.outputFormat = elem.getTextContent();
  }

  private static void readDBConnectionElement(Configuration config, Element elem) {
    NodeList authentifications = elem.getElementsByTagNameNS(D2R.D2RNS, D2R.DBAUTHENTIFICATION_ELEMENT);
    if (authentifications.getLength() > 0)
      readDBAuthentificationElement(config, (Element)authentifications.item(0));

    // jdbcDSN and jdbcDriver are required attributes
    config.jdbc = elem.getAttribute(D2R.DBCONNECTION_JDBC_DSN_ATTRIBUTE);
    config.jdbcDriver = elem.getAttribute(D2R.DBCONNECTION_JDBC_DRIVER_ATTRIBUTE);

  }

  private static void readDBAuthentificationElement(Configuration config, Element elem) {
    config.databaseUsername = elem.getAttribute(D2R.DBAUTHENTIFICATION_USERNAME_ATTRIBUTE);
    String password = elem.getAttribute(D2R.DBAUTHENTIFICATION_PASSWORD_ATTRIBUTE);
    if (password == null) password = "";
    config.databasePassword = password;
  }

  private static void readComplexTypeNamespace(Configuration config, Element elem) {
    config.getNamespaces().put(elem.getAttribute(D2R.NAMESPACE_PREFIX_ATTRIBUTE),
        elem.getAttribute(D2R.NAMESPACE_NAMESPACE_ATTRIBUTE));
  }
}