package de.fuberlin.wiwiss.d2r;

import java.util.*;
import java.io.*;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.unipassau.medspace.util.SqlUtil;
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
  private boolean mapLoaded;

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  /** JDBC Connection used to retrieve data */
  private Connection connection = null;

  /** Classpath of JDBC Driver (JAR) used to establish the connection */
  private URL driverClasspath = null;


  public D2rProcessor() {
    initialize();
  }

  private void initialize() {
    config = ConfigurationReader.createDefaultConfig();
    mapLoaded = false;
  }

  /**
   * Command line interface. Parameters:<BR> <UL><LI>-map:filename  : path/filename of the D2R mapping file.</LI>
   * <LI>-output:filename   : path/filename of the output file.</LI>
   * <LI>-format:name 	  : Output format. Possible values are RDF/XML, RDF/XML-ABBREV, N-TRIPLES and N3.</LI> </UL>
   * @param args line Arguments.
   */
  public static void main(String[] args) {

    String outputFilename = null;
    String mapFilename = null;
    String fileFormat = null;

    try {

      // process parameters
      for (String arg : args) {
        if (arg.substring(0, 8).equals("-format:")) {
          fileFormat = arg.substring(8).trim();
        } else if (arg.substring(0, 5).equals("-map:")) {
          mapFilename = arg.substring(5).trim();
        } else if (arg.substring(0, 8).equals("-output:")) {
          outputFilename = arg.substring(8).trim();
        } else {
          throw new D2RException("Unknown command line argument: " + arg);
        }
      }

      //initialize log4j (required by main class)
      // Get the url of the log configuration from jar
      URL logConfigAddress = (D2rProcessor.class).getResource("/log4j-d2r.xml");

      if (logConfigAddress != null) {
        // Configurate using the log file
        DOMConfigurator.configure(logConfigAddress);
      }
      else {
        // Just use the basic configurator
        BasicConfigurator.configure();
      }

      //BEGIN PROCESSING
      log.debug("Processing D2R Map: " + mapFilename + " ....");

      // generate processor instance, process map
      D2rProcessor processor = new D2rProcessor();
      processor.processMap(fileFormat, mapFilename, outputFilename);

      log.debug("processing complete.");
    }
    catch (Exception ex) {

      log.error("Could not process D2R Map.", ex);
    }
  }

  public String getOutputFormat() {
    return config.getOutputFormat();
  }

  /**
   * Processes a D2R map. Main processing method. Processes a D2R Map
   * (mapFilename) and outputs it to the specified (outputFileName) file in the
   * chosen format (format).
   *
   * @param format The format (e.g. Turtle or RDF/XML) to write the result of the mapping
   * @param mapFilename The file that specifies the mapping
   * @param outputFilename The target file to write the result of the mapping
   * @throws D2RException Thrown if the mapping couldn't properly be done.
   */
  private void processMap(String format, String mapFilename,
                          String outputFilename) throws D2RException {

    //validate input file path
    if (mapFilename != null) {

      //input and output
      File file = new File(mapFilename);

      //default output options
      config.setSaveAs("System.out");
      OutputStream outputStream = System.out;

      try {

        //check if a format has been specified
        if (format != null) {
          config.setOutputFormat(format);
        } else {
          config.setOutputFormat(D2R.STANDARD_OUTPUT_FORMAT);
        }

        //default output is System.out
        if (outputFilename != null) {
          config.setSaveAs(outputFilename);

          //output to file
          File outFile = new File(config.getSaveAs());
          outputStream = new FileOutputStream(outFile);
        }
      }
      catch (FileNotFoundException fileException) {

        throw new D2RException("Could not get OutputStream for: " +
                               outputFilename, fileException);
      }

      //PROCESS MAP USING INPUT AND OUTPUT (format has been set)
      this.processMap(file, outputStream);
    }
    else {

      throw new D2RException("A D2R map has to be specified with the " +
                             "command line argument -map:filename.");
    }
  }

  /**
   * Processes a D2R map. Processes Map from input (inputFile) and outputs
   * to an OutputStream (outStream) in the pre-set format.
   *
   * @param inputFile The D2R map
   * @param outStream The destination for writing the result of the mapping
   * @throws D2RException Thrown if the mapping couldn't properly be done.
   */
  private void processMap(File inputFile, OutputStream outStream)
      throws D2RException {

    // validate input
    if (inputFile != null) {

      //used to write output to an output stream
      PrintWriter out = null;

      try {

        // Read D2R Map file
        this.readMap(inputFile);

        // Generate instances for all maps
        String output = this.generateAllInstancesAsString();

        // write model to output
        out = new PrintWriter(outStream);
        out.println(output);
      } catch (D2RException e) {

        //re-throw any errors as a D2RException that can be displayed to the user
        throw new D2RException(e.getMessage(), e);
      } finally {

        //close the output stream
        if (out != null) {out.close();}

        //reset cached connection (assume it has been closed)
        setConnection(null);
      }
    }
    else {

      throw new D2RException("Could not process D2RMap. File is null.");
    }
  }

  /**
   * Processes a D2R map (as a Document object) and outputs the results to the
   * specified Model (jena Model).
   *
   * @param driverClasspath The URL of the driver class path
   * @param document The D2R map
   * @param model Output model for writing results of the mapping to.
   * @throws D2RException Thrown if the mapping couldn't properly be done.
   */
  public void processMap(URL driverClasspath, Document document,
                         Model model) throws D2RException {

    // validate arguments.
    if ( (document != null)
        && (model != null)) {

      //set the connection (used elsewhere)
      this.setDriverClasspath(driverClasspath);

      try {

        //use standard format
        config.setOutputFormat(D2R.STANDARD_OUTPUT_FORMAT);

        //Read D2R Map (Document)
        this.readMap(document);

        //process map and output results to the supplied model
        this.outputToModel(model);
      }
      catch (IOException ex) {

        //re-throw any errors as a D2RException that can be displayed to the user
        throw new D2RException(ex.getMessage(), ex);
      }
    }
    else {

      throw new D2RException("Cannot process D2R map. Document and/or Model " +
                             "can not be null.");
    }
  }

  /**
   * Processes a D2R map. Processes a D2R Map from input (inputFile) and outputs
   * directly to a Jena model (model).
   *
   * @param inputFile The input file
   * @param model The model to save the result of the input file to.
   * @throws D2RException Thrown if the mapping couldn't properly be done.
   */
  public void processMap(File inputFile, Model model)
      throws D2RException {

    // Check if a map has been specified first.
    if (inputFile != null) {

      try {

        //Read D2R Map file
        this.readMap(inputFile);

        //output to the jena model
        this.outputToModel(model);
      }
      catch (java.lang.Throwable ex) {

        //re-throw any errors as a D2RException that can be displayed to the user
        throw new D2RException(ex.getMessage(), ex);
      }
    }
    else {

      throw new D2RException("Could not process D2RMap. File is null.");
    }
  }

  /**
   * Processes the D2R map and returns all generated instances.
   * @return RDF, N3 or N-Triples depending on the processor instruction d2r:outputFormat.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   * (see {@link #readMap(File)}, {@link #readMap(String)}, {@link #readMap(Document)})
   */
  private String generateAllInstancesAsString() throws D2RException {

    // Check if a map is loaded.
    if (!this.mapLoaded) {
      throw new D2RException(
          "A D2R map has to be read before calling generateAllInstancesAsString().");
    }

    try {
      // Clear model
      this.model = null;
      this.model = ModelFactory.getInstance().createDefaultModel();
    } catch (FactoryException e) {
      throw new D2RException("Couldn't get default Model from the ModelFactory.", e);
    }

    // Generate instances for all maps
    this.generateInstancesForAllMaps();

    //toString
    return this.serialize();
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   * (see {@link #readMap(File)}, {@link #readMap(String)}, {@link #readMap(Document)})
   */
  public Model generateAllInstancesAsModel() throws D2RException {

    // Check if a map is loaded.
    if (!this.mapLoaded) {
      throw new D2RException("A D2R map has to be read before calling generateAllInstancesAsModel().");
    }

    try {

      // Clear model
      this.model = ModelFactory.getInstance().createDefaultModel();
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

    // Check if a map is loaded and ensure parameter is valid
    if (!this.mapLoaded) {
      throw new D2RException("A D2R map has to be read before calling " +
          "generateAllInstancesAsModel().");
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

  /** Frees all resources. */
  public void dropMap() {
    this.initialize();
  }

  /** Serializes model to string and includes the content of the d2r:Prepend and d2r:Postpend statements. */
  private String serialize() throws D2RException {
    StringBuilder ser = new StringBuilder();
    if (config.getPrepend() != null) ser.append(config.getPrepend());
    ser.append(this.modelToString());
    if (config.getPostpend() != null) ser.append(config.getPostpend());
    return ser.toString();
  }

  /**
   * Reads an D2R Map from the filesystem.
   * @param filename of the D2R Map
   */
  public void readMap(String filename) throws IOException, D2RException {

    //D2RMap file
    File file = new File(filename);

    //read the D2RMap file
    this.readMap(file);
  }

  /**
   * Reads a D2R Map from File.
   * @param file The file of the D2R Map
   */
  private void readMap(File file) throws D2RException {

    //parsed file
    Document document;

    try {

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      // Select type of parser
      //factory.setValidating(true);
      factory.setNamespaceAware(true);

      // Read document into DOM
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(file);

      //read the Document
      this.readMap(document);
    }
    catch (SAXParseException spe) {
      throw new D2RException("Error while parsing XML file: " + "line " +
                            spe.getLineNumber() +
                            ", uri: " + spe.getSystemId() + ", reason: " +
                            spe.getMessage(), spe);
    }
    catch (SAXException sxe) {
      throw new D2RException("Error while parsing XML file: ", sxe);
    }
    catch (ParserConfigurationException pce) {
      throw new D2RException("Error while building XML parser: ", pce);
    } catch (IOException e) {
      throw new D2RException("IO Error while parsing the map: ", e);
    }
  }

  /**
   * Reads a D2R Map as a Document.
   * @param document the file of the D2R Map
   */
  private void readMap(Document document) throws IOException, D2RException {

    //read the document object
    if (document == null) return;
    ConfigurationReader.readConfig(document, config);
    mapLoaded = true;
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
      String url = this.getJdbc();

      //make a new connection
      if (url == null || url.equals("")) {
        throw new D2RException("Could not connect to database because of missing URL.");
      }

      //Driver used to establish connection
      Driver driver = this.createDriver();

      if (driver == null) {
        throw new D2RException("Could not establish Connection. Cannot obtain Driver.");
      }

      log.debug("Creating new connection. URL: " + url);

      //use the Driver to establish a connection
      Properties connectionProperties = new Properties();

      //add the username and password to the properties
      String username = "";
      String password = "";
      if (this.getDatabaseUsername() != null) {
        username = this.getDatabaseUsername();
      }
      if (this.getDatabasePassword() != null) {
        password = this.getDatabasePassword();
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
   * Creates a new JDBC Driver from this Object's Connection properties.
   *
   * @throws D2RException Thrown if an error occurs while creating the Driver
   * @return Driver The JDBC driver to the datasource. NOTE: It is guaranteed, that the result is not null
   */
  private Driver createDriver() throws D2RException {
    //value to be returned
    Driver driver;

    //name of Driver Class
    String driverClass = getJdbcDriver();

    //get required information
    if (driverClass == null) {
      throw new D2RException("Could not connect to database because of " +
          "missing Driver.");
    }

    try {
      //if there is a classpath supplied, use it to instantiate Driver
      if (this.getDriverClasspath() != null) {

        //dynamically load and instantiate Driver from the classPath URL
        driver = DriverFactory.getInstance().getDriverInstance(driverClass,
            this.getDriverClasspath());
      }
      else {

        //attempt to load and instantiate Driver from the current classpath
        driver = DriverFactory.getInstance().getDriverInstance(driverClass);
      }
    }
    catch (FactoryException factoryException) {

      throw new D2RException("Could not instantiate Driver class.",
                             factoryException);
    }

    if (driver == null)
      throw new D2RException("Driver is supposed to be != null! Fix the bug!");

    return driver;
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
   * Sets the driver classpath member.
   *
   * @param classpath The class path to the driver in URL notation
   */
  private void setDriverClasspath(URL classpath) {

    this.driverClasspath = classpath;
  }

  /**
   * Returns the classpath that is used to load JDBC Drivers.
   *
   * @return URL
   */
  private URL getDriverClasspath(){

    return this.driverClasspath;
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
   * Returns the JDBC data source name.
   * @return jdbcDSN
   */
  private String getJdbc() {
    return config.getJdbc();
  }

  /**
   * Returns the JDBC driver.
   * @return jdbcDriver
   */
  private String getJdbcDriver() {
    return config.getJdbcDriver();
  }

  /**
   * Returns the database username.
   * @return username
   */
  private String getDatabaseUsername() {
    return config.getDatabaseUsername();
  }

  /**
   * Returns the database password.
   * @return password
   */
  private String getDatabasePassword() {
    return config.getDatabasePassword();
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