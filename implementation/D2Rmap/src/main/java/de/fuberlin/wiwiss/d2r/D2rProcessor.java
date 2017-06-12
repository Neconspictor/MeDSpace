package de.fuberlin.wiwiss.d2r;

import java.util.*;
import java.io.*;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import de.unipassau.medspace.util.SqlUtil;
import de.unipassau.medspace.util.XmlUtil;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.*;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import java.net.URL;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.*;
import java.sql.*;
import java.util.Map.Entry;

import de.fuberlin.wiwiss.d2r.factory.ModelFactory;
import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.fuberlin.wiwiss.d2r.exception.FactoryException;
import de.fuberlin.wiwiss.d2r.factory.DriverFactory;

/**
 * D2R processor exports data from a RDBMS into an RDF model using a D2R MAP.
 * D2R MAP is a declarative, XML-based language to describe mappings between the relational
 * database model and the graph-based RDF data model. The resulting model can be serialized as RDF, N3, N-TRIPLES or exported
 * directly as Jena model. The processors is compliant with all relational databases offering JDBC or ODBC access.
 * The processor can be used in a servlet environment to dynamically publish XHTML pages
 * containing RDF, as a database connector in applications working with Jena models or as a command line tool.
 * The D2R Map language specification and usage examples are found at
 * http://www.wiwiss.fu-berlin.de/suhl/bizer/d2rmap/D2Rmap.htm.
 *
 * <BR><BR>History:
 * <BR>18-05-2017   : Updated for Java 8; removed unsafe operations; all-embracing refactoring
 * <BR>07-21-2004   : Process map methods added.
 * <BR>07-21-2004   : Connection and driver accessors added. 
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 *
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3.1
 */
public class D2rProcessor {
  private Configuration config;
  private Model model;

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  /** JDBC Connection used to retrieve data */
  private Connection connection = null;

  private QueryManager queryManager;


  public D2rProcessor(Configuration config, QueryManager queryManager) {
    assert config != null;
    assert queryManager != null;

    this.config = config;
    this.queryManager = queryManager;
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   */
  public Model generateTestAsModel() throws D2RException {
    try {
      clear();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }

    // Generate instances for all maps
    this.generateTestMaps();

    // add namespaces
    for (Entry<String, String> ent : config.getNamespaces().entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //Return model
    return this.model;
  }

  private void clear() throws FactoryException {
    // Clear model
    this.model = null;
    this.model = ModelFactory.getInstance().createDefaultModel();

    // clear maps
    for (D2RMap map : getMaps())
      map.clear();
  }

  private void generateTestMaps() throws D2RException {
    D2RMap map = config.getMaps().elementAt(3);
      map.generateResources(this, new Vector<>());;
      map.generateResourceProperties(this);
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   */
  public Model generateAllInstancesAsModel() throws D2RException {
    try {
      clear();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }

    // Generate instances for all maps
    this.generateInstancesForAllMaps();

    // add namespaces
    for (Entry<String, String> ent : config.getNamespaces().entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //Return model
    return this.model;
  }

  /**
   * Processes the D2R map outputting the results to the "model" parameter.
   * NOTE: A map has to be loaded previously
   * @param model Model to save instances to. NOTE: The parameter mustn't to be null.
   * @throws D2RException Thrown if an error occurs
   * @throws NullPointerException Thrown if <code> model</code> is null
   */
  private void outputToModel(Model model) throws
      D2RException {

    // Backup the Model object (maintains model's state)
    Model originalModel = this.model;

    log.debug("Processing map to model.");

    if (model == null) {
      throw new NullPointerException("model mustn't be null");
    }

    // use model parameter
    this.model = model;

    // Generate instances for all maps
    this.generateInstancesForAllMaps();

    // add namespaces
    for (Entry<String, String> ent : config.getNamespaces().entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //reset model member
    this.model = originalModel;
  }

  /** Serializes model to string and includes the content of the d2r:Prepend and d2r:Postpend statements. */
  public String serialize() throws D2RException {
    StringBuilder ser = new StringBuilder();
    if (config.getPrepend() != null) ser.append(config.getPrepend());
    ser.append(this.modelToString());
    if (config.getPostpend() != null) ser.append(config.getPostpend());
    return ser.toString();
  }

  /** Generated instances for all D2R maps. */
  private void generateInstancesForAllMaps() throws D2RException {
    for (D2RMap map : config.getMaps())
      map.generateResources(this, new Vector<>());;
    for (D2RMap map : config.getMaps())
      map.generateResourceProperties(this);
  }
  /**
   * Uses a Jena writer to serialize model to RDF, N3 or N-TRIPLES.
   * @return serialization of model
   */
  private String modelToString() {
      StringWriter writer = new StringWriter();
    for (Entry<String, String> ent : config.getNamespaces().entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

      log.debug("Converting Model to String. outputFormat: " + config.getOutputFormat());

      this.model.write(writer, config.getOutputFormat());
      return writer.toString();
  }

  /**
   * If a valid Connection has previously been set/created, it will be returned.
   * Otherwise a new connection will be made and will be cached for the next
   * call.
   *
   * NOTE: It is assumed the connection will be closed (or set to null) when
   * it is no longer needed (processing is complete).
   *
   * @throws D2RException Thrown if the connection to the datasource couldn't be retrieved
   * @return Connection
   */
  Connection getConnection() throws D2RException {
    Connection con;

    try {

      //early exit when a connection already exists
      if ( (this.connection != null) && (!this.connection.isClosed())) {
        log.debug("Retrieving existing connection.");
        return this.connection;
      }

      // Connect to database
      String url = config.getJdbc();

      //make a new connection
      if (url == null || url.equals("")) {
        throw new D2RException("Could not connect to database because of missing URL.");
      }

      //Driver used to establish connection
      Driver driver = SqlUtil.createDriver(config.getJdbcDriver());

      if (driver == null) {
        throw new D2RException("Could not establish Connection. Cannot obtain Driver.");
      }

      log.debug("Creating new connection. URL: " + url);

      //use the Driver to establish a connection
      Properties connectionProperties = new Properties();

      //add the username and password to the properties
      String username = "";
      String password = "";
      if (config.getDatabaseUsername() != null) {
        username = config.getDatabaseUsername();
      }
      if (config.getDatabasePassword() != null) {
        password = config.getDatabasePassword();
      }

      connectionProperties.setProperty("user", username);
      connectionProperties.setProperty("password", password);

      //connect to the URL using the Driver
      con = driver.connect(url, connectionProperties);

      //cache connection
      this.setConnection(con);
    }
    catch (SQLException | D2RException ex) {
      //if (con != null) SqlUtil.closeSilently(con);
      String message = "Exception caught while trying to connect: ";
      if (ex instanceof  SQLException) {
        message += SqlUtil.unwrapMessage((SQLException) ex);
      } else {
        message += ex.getMessage();
      }
      throw new D2RException(message, ex);
    }

    log.debug("Returning connection: " + con);
    return con;
  }

  /**
   * Sets the Connection member.
   *
   * @param connection Connection
   */
  private void setConnection(Connection connection) {

    this.connection = connection;
  }

  /**
   * Returns an vector containing all D2R maps.
   * @return Vector with all maps.
   */
  private Vector<D2RMap> getMaps() {
    return config.getMaps();
  }

  /**
   * Returns the D2R map identified by the id parameter.
   * @return D2R Map.
   */
  D2RMap getMapById(String id) {
    for (D2RMap map : this.getMaps()) {
      if (map.getId().equals(id)) {
        return map;
      }
    }
    return null;
  }

  /**
   * Returns an HashMap containing all translation tables.
   * @return Vector with all maps.
   */
  HashMap<String, TranslationTable> getTranslationTables() {
    return config.getTranslationTables();
  }

  /**
   * Returns a reference to the Jena model.
   * @return Jena Model.
   */
  protected Model getModel() {
    return this.model;
  }



  /**
   * Translates a qName to an URI using the namespace mapping of the D2R map.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  @SuppressWarnings("SpellCheckingInspection")
  String getNormalizedURI(String qName) {
    String prefix = D2rUtil.getNamespacePrefix(qName);
    String uriPrefix = config.getNamespaces().get(prefix);
    if (uriPrefix != null) {
      String localName = D2rUtil.getLocalName(qName);
      return uriPrefix + localName;
    }
    else {
      return qName;
    }
  }
}