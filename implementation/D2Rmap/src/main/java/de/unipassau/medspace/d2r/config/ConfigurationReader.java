package de.unipassau.medspace.d2r.config;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.bridge.Bridge;
import de.unipassau.medspace.d2r.bridge.DatatypePropertyBridge;
import de.unipassau.medspace.d2r.bridge.ObjectPropertyBridge;
import de.unipassau.medspace.d2r.config.parsing.*;
import de.unipassau.medspace.d2r.exception.D2RException;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
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

  /**
   * TODO
   */
  private RDFProvider provider;

  /**
   * TODO
   */
  private RDFFactory rdfFactory;


  /**
   * Constructs a new ConfigurationReader.
   */
  public ConfigurationReader(RDFProvider provider) {
    this.provider = provider;
    this.rdfFactory = provider.getFactory();
  }

  /**
   * Reads an D2R Map from the filesystem.
   * @param filename of the D2R Map
   * @return The configuration file
   * @throws IOException if an error occurs
   */
  public Configuration readConfig(String filename) throws IOException {
    try {
      return parse(filename);
    } catch (ClassNotFoundException | D2RException | JAXBException | SAXException | URISyntaxException e) {
      throw new IOException("Error while parsing XML file: ", e);
    }
  }

  /**
   * TODO
   * @param filename
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws URISyntaxException
   * @throws D2RException
   * @throws JAXBException
   */
  private Configuration parse(String filename) throws
      IOException,
      ClassNotFoundException,
      URISyntaxException,
      D2RException,
      JAXBException,
      SAXException {

    RootParsing configRoot = new Parser().parse(filename);

    Configuration config = createDefaultConfig();
    List<NamespaceParsing> namespaceParsings = configRoot.getNamespace();
    for (NamespaceParsing namespace : namespaceParsings) {
      Namespace converted = convert(namespace);
      config.getNamespaces().put(namespace.getPrefix(), converted);
    }

    DBConnectionParsing parsedConnection = configRoot.getDBConnection();

    addDBConnection(parsedConnection, config);

    List<ClassMapParsing> mapsParsing = configRoot.getClassMap();

    for (ClassMapParsing mapParsing : mapsParsing) {
      addMap(mapParsing, config);
    }

    for (ClassMapParsing mapParsing : mapsParsing) {
      addBridges(mapParsing, config);
    }

    return config;
  }

  /**
   * TODO
   * @param mapParsing
   * @param config
   * @throws D2RException
   */
  private void addBridges(ClassMapParsing mapParsing, Configuration config) throws D2RException {

    List<D2rMap> maps = config.getMaps();
    List<BridgeParsing> bridgesParsing = mapParsing.getDataTypePropertyBridgeOrObjectPropertyBridge();

    for (BridgeParsing bridgeParsing : bridgesParsing) {

      String pattern = bridgeParsing.getPattern();
      String property = bridgeParsing.getProperty();

      Bridge bridge;

      if (bridgeParsing instanceof DataTypePropertyBridgeParsing) {
        DataTypePropertyBridgeParsing dataTypeBridgeParsing = (DataTypePropertyBridgeParsing) bridgeParsing;
        String dataType = dataTypeBridgeParsing.getDataType();
        String lang = dataTypeBridgeParsing.getLang();

        DatatypePropertyBridge datatypeBridge = new DatatypePropertyBridge(provider.getFactory());
        datatypeBridge.setDataType(dataType);
        datatypeBridge.setLangTag(lang);
        bridge = datatypeBridge;

      } else {

        ObjectPropertyBridgeParsing objectBridgeParsing = (ObjectPropertyBridgeParsing)bridgeParsing;
        String refferedClass = objectBridgeParsing.getReferredClass();
        String referredColumns = objectBridgeParsing.getReferredColumns();

        ObjectPropertyBridge objectBridge = new ObjectPropertyBridge(
            rdfFactory,
            refferedClass,
            maps);
        objectBridge.setReferredColumns(referredColumns);

        bridge = objectBridge;
      }

      bridge.setPattern(pattern);
      bridge.setPropertyQName(property);
      D2rMap map = getMapByID(mapParsing.getId(), maps);
      map.addBridge(bridge);
    }
  }

  /**
   * TODO
   * @param parsedConnection
   * @param config
   * @throws URISyntaxException
   * @throws ClassNotFoundException
   */
  private void addDBConnection(DBConnectionParsing parsedConnection, Configuration config) throws
      URISyntaxException,
      ClassNotFoundException {

    config.setPoolSize(parsedConnection.getPoolSize());
    config.setJdbc(new URI(parsedConnection.getJdbcDSN()));

    Class jdbcDriver = Class.forName(parsedConnection.getJdbcDriver());
    config.setJdbcDriver(jdbcDriver);

    DBAuthentificationParsing authentification = parsedConnection.getDBAuthentification();
    config.setDatabaseUsername(authentification.getUsername());
    config.setDatabasePassword(authentification.getPassword());


    List<DataSourcePropertyParsing> properties = parsedConnection.getDataSourceProperty();
    for (DataSourcePropertyParsing property : properties) {
      String name  = property.getName();
      String value = property.getValue();
      config.getDataSourceProperties().add(new Pair<>(name, value));
    }
  }

  /**
   * TODO
   * @param mapParsing
   * @param config
   * @throws D2RException
   */
  private void addMap(ClassMapParsing mapParsing, Configuration config) throws D2RException {
    D2rMap map = new D2rMap(rdfFactory);

    String id = mapParsing.getId();
    validateD2RMapId(id, config.getMaps());
    map.setId(id);

    String sqlQuery = mapParsing.getSql();
    validateSqlQuery(sqlQuery);
    map.setSql(sqlQuery);

    String resourceIdPattern = mapParsing.getResourceIdPattern();

    //check that resource id columns aren't empty, has id creation
    //D2rMaps otherwise doesn't work!
    if (resourceIdPattern == null || resourceIdPattern.equals("")) {
      throw new D2RException("resourceIdColumns mustn't be empty!");
    }

    map.setResourceIdPattern(resourceIdPattern);

    //map.getResourceIdColumns().clear(); //TODO test!

    config.getMaps().add(map);

    String type = mapParsing.getType();
    ObjectPropertyBridge typeProperty = new ObjectPropertyBridge(
        rdfFactory,
        map.getId(),
        config.getMaps());

    typeProperty.setPropertyQName("rdf:type");
    typeProperty.setPattern(type);
    map.addBridge(typeProperty);
  }

  /**
   * TODO
   * @param namespaceParsing
   * @return
   */
  private Namespace convert(NamespaceParsing namespaceParsing) {
    String prefix = namespaceParsing.getPrefix();
    String uri = namespaceParsing.getNamespace();
    return new Namespace(prefix, uri);
  }

  /**
   * Creates a new ConfigurationReader and initializes it with default values specified in {@link D2R}.
   * @return
   */
  private Configuration createDefaultConfig() {
    Configuration config = new Configuration();
    config.getNamespaces().put(D2R.RDFNS_PREFIX, new Namespace(D2R.RDFNS_PREFIX, D2R.RDFNS));

    String format = D2R.STANDARD_OUTPUT_FORMAT;

    if (!provider.isValid(format)) {
      throw new IllegalStateException("Default output language couldn't be mapped to a Lang object!");
    }

    return config;
  }

  /**
   * TODO
   * @param referredClassID
   * @param maps
   * @return
   * @throws D2RException
   */
  D2rMap getMapByID(String referredClassID, List<D2rMap> maps) throws D2RException {
// search the referred class on the base on its id
    for (D2rMap map : maps) {
      if (map.compareIdTo(referredClassID)) {
        return map;
      }
    }
    throw new D2RException("Id not in D2rMap list: " + referredClassID);
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