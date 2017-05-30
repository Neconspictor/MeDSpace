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
  private String saveAs;
  private String outputFormat;
  private String jdbc;
  private String jdbcDriver;
  private String databaseUsername;
  private String databasePassword;
  private String prepend;
  private String postpend;
  private Vector<Map> maps;
  private HashMap<String, TranslationTable> translationTables;
  private HashMap<String, String> namespaces;
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
    maps = new Vector<>();
    namespaces = new HashMap<>();
    namespaces.put(D2R.RDFNS_PREFIX, D2R.RDFNS);
    translationTables = new HashMap<>();
    mapLoaded = false;
    outputFormat = D2R.STANDARD_OUTPUT_FORMAT;
    saveAs = "StandardOut";
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
    return outputFormat;
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
      this.saveAs = "System.out";
      OutputStream outputStream = System.out;

      try {

        //check if a format has been specified
        if (format != null) {

          this.outputFormat = format;
        }
        else {

          this.outputFormat = D2R.STANDARD_OUTPUT_FORMAT;
        }

        //default output is System.out
        if (outputFilename != null) {

          this.saveAs = outputFilename;

          //output to file
          File outFile = new File(this.saveAs);
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

      throw new D2RException("Could not process Map. File is null.");
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
        this.outputFormat = D2R.STANDARD_OUTPUT_FORMAT;

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

      throw new D2RException("Could not process Map. File is null.");
    }
  }

  /**
   * Processes the D2R map and returns all generated instances.
   * @return RDF, N3 or N-Triples depending on the processor instruction d2r:outputFormat.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no Map was read before
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
    // Generate properties for all instances
    this.generatePropertiesForAllInstancesOfAllMaps();

    //toString
    return this.serialize();
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no Map was read before
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
    // Generate properties for all instances
    generatePropertiesForAllInstancesOfAllMaps();
    // add namespaces
    for (Entry<String, String> ent : this.namespaces.entrySet()) {
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

    // Generate properties for all instances
    this.generatePropertiesForAllInstancesOfAllMaps();

    // add namespaces
    for (Entry<String, String> ent : this.namespaces.entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //reset model member
    this.model = originalModel;
  }

  /**
   * Sets the output Format. Possible values are: RDF/XML, RDF/XML-ABBREV, N-TRIPLE, N3
   * @param format the Output format.
   */
  public void setOutputFormat(String format) {
    this.outputFormat = format;
  }

  /** Frees all resources. */
  public void dropMap() {
    this.initialize();
  }

  /** Serializes model to string and includes the content of the d2r:Prepend and d2r:Postpend statements. */
  private String serialize() throws D2RException {
    StringBuilder ser = new StringBuilder();
    if (this.prepend != null) ser.append(this.prepend);
    ser.append(this.modelToString());
    if (this.postpend != null) ser.append(this.postpend);
    return ser.toString();
  }

  /**
   * Reads an D2R Map from the filesystem.
   * @param filename of the D2R Map
   */
  public void readMap(String filename) throws IOException, D2RException {

    //Map file
    File file = new File(filename);

    //read the Map file
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
    if (document != null) {

      // Read namespaces
      NodeList list = document.getElementsByTagNameNS(D2R.D2RNS, "Namespace");
      int numNodes = list.getLength();
      for (int i = 0; i < numNodes; i++) {
        Element elem = (Element) list.item(i);
        this.namespaces.put(elem.getAttributeNS(D2R.D2RNS, "prefix"),
                            elem.getAttributeNS(D2R.D2RNS, "namespace"));
      }

      // Read database connection
      list = document.getElementsByTagNameNS(D2R.D2RNS, "DBConnection");
      Element elem = (Element) list.item(0);

      if (elem == null)
        throw new D2RException("No DBConnection was specified in the mapping");

      if (elem.hasAttributeNS(D2R.D2RNS,"jdbcDSN"))
        this.jdbc = elem.getAttributeNS(D2R.D2RNS, "jdbcDSN");
      if (elem.hasAttributeNS(D2R.D2RNS, "jdbcDriver"))
        this.jdbcDriver = elem.getAttributeNS(D2R.D2RNS, "jdbcDriver");
      if (elem.hasAttributeNS(D2R.D2RNS, "username"))
        this.databaseUsername = elem.getAttributeNS(D2R.D2RNS, "username");
      if (elem.hasAttributeNS(D2R.D2RNS, "password"))
        this.databasePassword = elem.getAttributeNS(D2R.D2RNS, "password");

      // Read prepend/postpend
      list = document.getElementsByTagNameNS(D2R.D2RNS, "Prepend");
      if (list.getLength() != 0) {
        if (list.getLength() != 1)
          throw new D2RException("Only one Prepend statement allowed.");
        elem = (Element) list.item(0);
        this.prepend = elem.getFirstChild().getNodeValue();
      }
      list = document.getElementsByTagNameNS(D2R.D2RNS, "Postpend");
      if (list.getLength() != 0) {
        if (list.getLength() != 1)
          throw new D2RException("Only one Postpend statement allowed.");
        elem = (Element) list.item(0);
        this.postpend = elem.getFirstChild().getNodeValue();
      }

      // Read processor messages
      list = document.getElementsByTagNameNS(D2R.D2RNS, "ProcessorMessage");
      numNodes = list.getLength();
      for (int i = 0; i < numNodes; i++) {
        elem = (Element) list.item(i);
        if (elem.hasAttributeNS(D2R.D2RNS, "saveAs"))
          this.saveAs = elem.getAttributeNS(D2R.D2RNS, "saveAs").trim();
        if (elem.hasAttributeNS(D2R.D2RNS, "outputFormat"))
          this.outputFormat = elem.getAttributeNS(D2R.D2RNS, "outputFormat").
              trim();
      }

      // Read translation tables
      list = document.getElementsByTagNameNS(D2R.D2RNS, "ResultInstance");
      numNodes = list.getLength();
      for (int i = 0; i < numNodes; i++) {
        elem = (Element) list.item(i);
        String tableId = elem.getAttributeNS(D2R.D2RNS, "id").trim();
        TranslationTable table = new TranslationTable();
        // Read Translations
        NodeList translationList = elem.getElementsByTagNameNS(D2R.D2RNS,
            "Translation");
        int numTranslationNodes = translationList.getLength();
        for (int j = 0; j < numTranslationNodes; j++) {
          Element translation = (Element) translationList.item(j);
          table.put(translation.getAttributeNS(D2R.D2RNS, "key").trim(),
                    translation.getAttributeNS(D2R.D2RNS, "value").trim());
        }
        this.translationTables.put(tableId, table);
      }

      // Read maps
      list = document.getElementsByTagNameNS(D2R.D2RNS, "ClassMap");
      numNodes = list.getLength();
      for (int i = 0; i < numNodes; i++) {
        elem = (Element) list.item(i);
        Map cMap = new Map();
        // Read type attribute
        if (elem.hasAttributeNS(D2R.D2RNS, "type")) {
          cMap.setId(elem.getAttributeNS(D2R.D2RNS, "type").trim());
          // add rdf:type bridge TODO comment in
          ObjectPropertyBridge typeBridge = new ObjectPropertyBridge();
          typeBridge.setProperty("rdf:type");
          typeBridge.setValue(elem.getAttributeNS(D2R.D2RNS, "type").trim());
          cMap.addBridge(typeBridge);
        }
        // Read id attribute
        if (elem.hasAttributeNS(D2R.D2RNS, "id"))
          cMap.setId(elem.getAttributeNS(D2R.D2RNS, "id").trim());
          // Read sql attribute
        cMap.setSql(elem.getAttributeNS(D2R.D2RNS, "sql"));
        // Read groupBy attributes
        cMap.addGroupByFields(elem.getAttributeNS(D2R.D2RNS, "groupBy"));
        // Read uriPattern
        if (elem.hasAttributeNS(D2R.D2RNS, "uriPattern"))
          cMap.setUriPattern(elem.getAttributeNS(D2R.D2RNS, "uriPattern"));
        if (elem.hasAttributeNS(D2R.D2RNS, "uriColumn"))
          cMap.setUriColumn(elem.getAttributeNS(D2R.D2RNS, "uriColumn"));

          // Read datatype property mappings
        NodeList propertyList = elem.getElementsByTagNameNS(D2R.D2RNS,
            "DatatypePropertyBridge");
        int numPropertyNodes = propertyList.getLength();
        for (int j = 0; j < numPropertyNodes; j++) {
          Element propertyElement = (Element) propertyList.item(j);
          DatatypePropertyBridge propertyBridge = new DatatypePropertyBridge();
          propertyBridge.setProperty(propertyElement.getAttributeNS(D2R.D2RNS,
              "property").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "column"))
            propertyBridge.setColumn(propertyElement.getAttributeNS(D2R.D2RNS,
                "column").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "pattern"))
            propertyBridge.setPattern(propertyElement.getAttributeNS(D2R.
                D2RNS,
                "pattern").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "value"))
            propertyBridge.setValue(propertyElement.getAttributeNS(D2R.D2RNS,
                "value").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "translate"))
            propertyBridge.setTranslation(propertyElement.getAttributeNS(D2R.
                D2RNS, "translate").trim());
          if (propertyElement.hasAttributeNS(D2R.XMLNS, "lang"))
            propertyBridge.setXmlLang(propertyElement.getAttributeNS(D2R.
                XMLNS,
                "lang").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "datatype"))
            propertyBridge.setDatatype(propertyElement.getAttributeNS(D2R.
                D2RNS,
                "datatype").trim());
          cMap.addBridge(propertyBridge);
        }

        // Read object property mappings
        propertyList = elem.getElementsByTagNameNS(D2R.D2RNS,
            "ObjectPropertyBridge");
        numPropertyNodes = propertyList.getLength();
        for (int j = 0; j < numPropertyNodes; j++) {
          Element propertyElement = (Element) propertyList.item(j);
          ObjectPropertyBridge propertyBridge = new ObjectPropertyBridge();
          propertyBridge.setProperty(propertyElement.getAttributeNS(D2R.D2RNS,
              "property").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "column"))
            propertyBridge.setColumn(propertyElement.getAttributeNS(D2R.D2RNS,
                "column").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "pattern"))
            propertyBridge.setPattern(propertyElement.getAttributeNS(D2R.
                D2RNS,
                "pattern").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "value"))
            propertyBridge.setValue(propertyElement.getAttributeNS(D2R.D2RNS,
                "value").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "translate"))
            propertyBridge.setTranslation(propertyElement.getAttributeNS(D2R.
                D2RNS, "translate").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "referredClass"))
            propertyBridge.setReferredClass(propertyElement.getAttributeNS(
                D2R.
                D2RNS, "referredClass").trim());
          if (propertyElement.hasAttributeNS(D2R.D2RNS, "referredGroupBy"))
            propertyBridge.setReferredGroupBy(propertyElement.getAttributeNS(
                D2R.D2RNS, "referredGroupBy").trim());
          cMap.addBridge(propertyBridge);
        }
        maps.add(cMap);
      }

      mapLoaded = true;
    }
  }

  /** Generated instances for all D2R maps. */
  private void generateInstancesForAllMaps() throws D2RException {
    for (Map map : maps) {
      map.generateResources(this);
    }
  }

  /**
   * Uses a Jena writer to serialize model to RDF, N3 or N-TRIPLES.
   * @return serialization of model
   */
  private String modelToString() {
      StringWriter writer = new StringWriter();
    for (Entry<String, String> ent : this.namespaces.entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

      log.debug("Converting Model to String. outputFormat: " + this.outputFormat);

      this.model.write(writer, this.outputFormat);
      return writer.toString();
  }

  /** Generated properties for all instances of all D2R maps. */
  private void generatePropertiesForAllInstancesOfAllMaps() throws D2RException {
    for (Map map : maps) {
      map.generatePropertiesForAllInstances(this);
    }
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
      if (ex instanceof  SQLException) message += SqlUtil.unwrapMessage((SQLException) ex);
      else message += ex.getMessage();
      throw new D2RException(message);
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
  private Vector<Map> getMaps() {
    return maps;
  }

  /**
   * Returns the D2R map identified by the id parameter.
   * @return D2R Map.
   */
  Map getMapById(String id) {
    for (Map  map : this.getMaps()) {
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
    return translationTables;
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
    return this.jdbc;
  }

  /**
   * Returns the JDBC driver.
   * @return jdbcDriver
   */
  private String getJdbcDriver() {
    return this.jdbcDriver;
  }

  /**
   * Returns the database username.
   * @return username
   */
  private String getDatabaseUsername() {
    return this.databaseUsername;
  }

  /**
   * Returns the database password.
   * @return password
   */
  private String getDatabasePassword() {
    return this.databasePassword;
  }

  /**
   * Translates a qName to an URI using the namespace mapping of the D2R map.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  String getNormalizedURI(String qName) {
    String prefix = D2rUtil.getNamespacePrefix(qName);
    String URIprefix = this.namespaces.get(prefix);
    if (URIprefix != null) {
      String localName = D2rUtil.getLocalName(qName);
      return URIprefix + localName;
    }
    else {
      return qName;
    }
  }
}