package de.fuberlin.wiwiss.d2r;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medsapce.SQL.SQLQueryResultStream;
import de.unipassau.medsapce.SQL.SQLResultTuple;
import de.unipassau.medsapce.indexing.SQLIndex;
import de.unipassau.medspace.util.SqlUtil;
import de.unipassau.medspace.util.sql.SelectStatement;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.log4j.Logger;

import java.util.Map.Entry;

import de.fuberlin.wiwiss.d2r.factory.ModelFactory;
import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.fuberlin.wiwiss.d2r.exception.FactoryException;

import javax.sql.DataSource;

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
  private Vector<D2RMap> maps;
  private SQLIndex index;
  private HashMap<String, String> namespaces;
  private URINormalizer normalizer;

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  private DataSourceManager dataSourceManager;


  public D2rProcessor(Configuration config, DataSourceManager dataSourceManager) throws D2RException {
    assert config != null;
    assert dataSourceManager != null;

    maps = config.getMaps();
    namespaces = config.getNamespaces();

    // we don't want others to change the state of the processor
    config.setMaps(null);
    config.setNamespaces(null);

    this.dataSourceManager = dataSourceManager;

    index = null;
    if (config.isIndexUsed()) {
     try {
       String directory = config.getIndexDirectory().toString();
       index = SQLIndex.create(directory);
     } catch (IOException e) {
       log.error(e);
       throw new D2RException("Couldn't create index!");
     }
    }

    for (D2RMap map : maps) {
      map.init(dataSourceManager.getDataSource(), maps);
    }

    normalizer = URI -> getNormalizedURI(URI);
  }


  public Model doKeywordSearch(List<String> keywords) throws D2RException {

    Model model = null;

    try {
      clear();
      model = ModelFactory.getInstance().createDefaultModel();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }


    // add namespaces
    for (Entry<String, String> ent : namespaces.entrySet()) {
      model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    // Generate instances for all maps
    for (D2RMap map : maps) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns);
      query.addTemporaryCondition(keywordCondition);
      generateResources(map, dataSourceManager.getDataSource(), new ArrayList<>());
    }

    //Return model
    return model;
  }

  /**
   * Generates all resources for the specified map.
   */
  public void generateResources(D2RMap map, DataSource dataSource,
                                List<String> conditionList) throws D2RException {
    String query = map.getQuery().toString();
    String ucQuery = query.toUpperCase();
    if (ucQuery.contains("UNION"))
      throw new D2RException("SQL statement should not contain UNION: " + query);

    //generate resources using the Connection
    generateResources(map, dataSource, query);
  }

  /**
   * Generates all resources for the specified map.
   * @param  dataSource The database connection.
   */
  private void generateResources(D2RMap map, DataSource dataSource, String query) throws D2RException {

    if (log.isDebugEnabled()) {
      log.debug("Generating resources for D2RProcessor: " + this);
    }

    // Create and execute SQL statement
    try(SQLQueryResultStream queryResult =
            SqlUtil.executeQuery(dataSource, query, 0, 10)) {

      for (SQLResultTuple tuple : queryResult) {
        ResultResource result = createResource(map, tuple);
      }
    }
    catch (SQLException ex) {
      String message = "SQL Exception caught: ";
      message += SqlUtil.unwrapMessage(ex);
      throw new D2RException(message);
    }
    catch (D2RException ex) {
      throw ex;
    }
    catch (java.lang.Throwable ex) {
      // Got some other type of exception.  Dump it.
      throw new D2RException("Error: " + ex.toString(), ex);
    }
  }

  private ResultResource createResource(D2RMap map, SQLResultTuple tuple)
      throws SQLException, D2RException {
    ResultResource currentTuple = new ResultResource();
    List<Triple> triples = new ArrayList<>();

    for (int i = 0; i < tuple.getColumnCount(); i++) {
      String columnName = tuple.getColumnName(i).toUpperCase();
      currentTuple.put(columnName, tuple.getValue(i));
    }

    Resource resource;

    // set instance id
    StringBuilder resourceIDBuilder = new StringBuilder();

    for (String aGroupBy : map.getResourceIdColumns()) {
      resourceIDBuilder.append(currentTuple.getValueByColmnName(aGroupBy));
    }
    String resourceID = resourceIDBuilder.toString();

    // define URI and generate instance
    String uri = map.getBaseURI() + resourceID;
    uri = getNormalizedURI(uri);
    resource = ResourceFactory.createResource(uri);

    if (resource == null || resourceID.equals("")) {
      log.warn("Warning: Couldn't create resource " + resourceID + " in map " + map.getId() +
          ".");
      return null;
    }

    currentTuple.setResource(resource);

    for (Bridge bridge : map.getBridges()) {
      // generate property
      Property prop = bridge.createProperty(this);
      RDFNode value = bridge.getValue(currentTuple, normalizer);
      if (prop != null && value != null) {
        Triple triple = Triple.create(resource.asNode(), prop.asNode(), value.asNode());
        triples.add(triple);
        System.out.println(triple);
      }
    }

    return currentTuple;
  }


  private void clear() throws FactoryException {
    // clear maps
    for (D2RMap map : maps)
      map.clear();
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   */
  public Model generateAllInstancesAsModel() throws D2RException {

    Model model = null;

    try {
      clear();
      model = ModelFactory.getInstance().createDefaultModel();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }

    // Generate instances for all maps
    generateInstancesForAllMaps();

    // add namespaces
    for (Entry<String, String> ent : namespaces.entrySet()) {
      model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //Return model
    return model;
  }


  /** Generated instances for all D2R maps. */
  private void generateInstancesForAllMaps() throws D2RException {
    for (D2RMap map : maps)
      generateResources(map, dataSourceManager.getDataSource(), new ArrayList<>());
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
    String uriPrefix = namespaces.get(prefix);
    if (uriPrefix != null) {
      String localName = D2rUtil.getLocalName(qName);
      return uriPrefix + localName;
    }
    else {
      return qName;
    }
  }

  public void someTestStuff(D2RMap map)
      throws D2RException, FactoryException {

    Model model = ModelFactory.getInstance().createDefaultModel();
    Graph graph = model.getGraph();
    PrefixMapping prefixMapping = model.getGraph().getPrefixMapping();
    Lang lang = Lang.N3;
    RDFFormat format = StreamRDFWriter.defaultSerialization(lang);
    if (format == null) {
      throw new D2RException("No serialization format for language: " + lang.getLabel());
    }
    OutputStream out  = System.out;
    StreamRDF rdfOut = StreamRDFWriter.getWriterStream(out, format);
    rdfOut.start();
    if(prefixMapping != null) {
      StreamOps.sendPrefixesToStream(prefixMapping, rdfOut);
    }

    Iterator<Triple> iter = graph.find((Node)null, (Node)null, (Node)null);
    StreamOps.sendTriplesToStream((Iterator)iter, rdfOut);
    rdfOut.finish();


    //NodeFactory
    //ResourceFactory
  }
}