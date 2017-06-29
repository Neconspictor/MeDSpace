package de.fuberlin.wiwiss.d2r;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import de.unipassau.medspace.SQL.SQLQueryResultStream;
import de.unipassau.medspace.indexing.SQLIndex;
import de.unipassau.medspace.rdf.D2rMapTripleStream;
import de.unipassau.medspace.rdf.MultiTripleStream;
import de.unipassau.medspace.rdf.TripleStream;
import de.unipassau.medspace.util.SqlUtil;
import de.unipassau.medspace.util.sql.SelectStatement;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
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
  private Vector<D2rMapper> maps;
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

    for (D2rMapper map : maps) {
      map.init(dataSourceManager.getDataSource(), maps);
    }

    normalizer = URI -> getNormalizedURI(URI);
  }


  /**
   * TODO
   * @param map
   * @param dataSource
   * @param conditionList
   * @return
   * @throws D2RException
   */
  public TripleStream createStream(D2rMapper map, DataSource dataSource,
                                   List<String> conditionList) throws D2RException {

    SelectStatement statement = map.getQuery();
    for (String condition : conditionList) {
      statement.addTemporaryCondition(condition);
    }

    String query = statement.toString();
    statement.reset();
    String ucQuery = query.toUpperCase();
    if (ucQuery.contains("UNION"))
      throw new D2RException("SQL statement should not contain UNION: " + query);

    //generate resources using the Connection
    return createStream(map, dataSource, query);
  }


  /**
   * TODO
   * @param keywords
   * @return
   * @throws D2RException
   */
  public MultiTripleStream doKeywordSearch(List<String> keywords) throws D2RException {

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

    MultiTripleStream result = new MultiTripleStream();

    // Generate instances for all maps
    for (D2rMapper map : maps) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns);
      query.addTemporaryCondition(keywordCondition);
      TripleStream stream = createStream(map, dataSourceManager.getDataSource(), new ArrayList<>());
      result.add(stream);
    }

    return result;
  }

  /** Generated instances for all D2R maps. */
  public MultiTripleStream getAllTriples() throws D2RException {
    MultiTripleStream result = new MultiTripleStream();
    for (D2rMapper map : maps)
      result.add(createStream(map, dataSourceManager.getDataSource(), new ArrayList<>()));

    return result;
  }

  /**
   * Translates a qName to an URI using the namespace mapping of the D2R map.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  @SuppressWarnings("SpellCheckingInspection")
  public String getNormalizedURI(String qName) {
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

  private void clear() throws FactoryException {
    // clear maps
    for (D2rMapper map : maps)
      map.clear();
  }

  /**
   * Creates a rdf triple stream from the specified query, that is executed on the specified datasource. For the rdf sql to rdf
   * mapping the specified D2rMapper is used. <p/>
   *
   * NOTE: The returned TripleStream won't be started, so no connection to the datasource will be established yet.
   * @param map The sql to rdf mapper
   * @param  dataSource The sql datasource.
   * @param query The query that should be executed on the datasource
   */
  private TripleStream createStream(D2rMapper map, DataSource dataSource, String query) {

    if (log.isDebugEnabled()) {
      log.debug("Create triple stream for map: " + map);
    }

    SQLQueryResultStream.QueryParams queryParams = new SQLQueryResultStream.QueryParams(dataSource, query);
    D2rMapTripleStream tripleStream = new D2rMapTripleStream(queryParams, map, normalizer);

    return tripleStream;
  }

  private void someTestStuff(D2rMapper map)
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

        /*tripleStream.start();
      OutputStream out  = System.out;
      Lang lang = Lang.TURTLE;
      RDFFormat format = StreamRDFWriter.defaultSerialization(lang);
      StreamRDF rdfOut = StreamRDFWriter.getWriterStream(out, format);
      rdfOut.start();
      for (Triple triple : tripleStream)
        rdfOut.triple(triple);
      rdfOut.finish();*/
  }
}