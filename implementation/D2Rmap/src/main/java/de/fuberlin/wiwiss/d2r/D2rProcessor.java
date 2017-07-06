package de.fuberlin.wiwiss.d2r;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medspace.SQL.SQLResultTuple;
import de.unipassau.medspace.SQL.SqlStream;
import de.unipassau.medspace.common.URINormalizer;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.indexing.SQLIndex;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.indexing.SearchResult;
import de.unipassau.medspace.indexing.SqlToDocumentStream;
import de.unipassau.medspace.rdf.DocToTripleStream;
import de.unipassau.medspace.rdf.SqlToTripleStream;
import de.unipassau.medspace.rdf.TripleStream;
import de.unipassau.medspace.util.FileUtil;
import de.unipassau.medspace.util.SqlUtil;
import de.unipassau.medspace.util.sql.SelectStatement;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.log4j.Logger;

import java.util.Map.Entry;

import de.fuberlin.wiwiss.d2r.factory.ModelFactory;
import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.fuberlin.wiwiss.d2r.exception.FactoryException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

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
  private List<D2rMap> maps;
  private SQLIndex index;
  private HashMap<String, String> namespaces;
  private URINormalizer normalizer;
  private HashMap<String, D2rMap> idToMap;

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
       index.open();
     } catch (IOException e) {
       log.error(e);
       throw new D2RException("Couldn't create index!");
     }
    }

    for (D2rMap map : maps) {
      map.init(dataSourceManager.getDataSource(), maps);
    }

    normalizer = URI -> getNormalizedURI(URI);

    idToMap = new HashMap<>();
    for (D2rMap map : maps) {
      idToMap.put(map.getId(), map);
    }
  }


  /**
   * Creates a rdf triple stream from the specified datasource. For the rdf sql to rdf
   * mapping the specified D2rMap is used. The triple stream will be resticted by the given conditionList
   * argument.<p/>
   *
   * NOTE: The returned TripleStream won't be started, so no connection to the datasource will be established yet.
   * @param map The sql to rdf mapper
   * @param  dataSource The sql datasource.
   * @param conditionList The query that should be executed on the datasource
   */
  public StreamFactory<Triple> createTripleStreamFactory(D2rMap map, DataSource dataSource,
                                                         List<String> conditionList) throws D2RException {

    SelectStatement statement = map.getQuery();
    for (String condition : conditionList) {
      statement.addTemporaryCondition(condition);
    }

    String query = statement.toString();
    statement.reset();

    //generate resources using the Connection
    return () -> {
      SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
      return new SqlToTripleStream(queryParams, map, normalizer);
    };
  }

  /**
   * TODO
   * @param map
   * @param dataSource
   * @param conditionList
   * @return
   * @throws D2RException
   */
  public StreamFactory<Document> createLuceneDocStreamFactory(D2rMap map, DataSource dataSource,
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


    SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
    StreamFactory<SQLResultTuple> factory = () -> {
      try {
        return new SqlStream(queryParams);
      } catch (SQLException e) {
        throw new IOException("Couldn't create stream to the sql datasource", e);
      }
    };

    //generate resources using the Connection
    return () -> new SqlToDocumentStream(factory, map);
  }


  public TripleStream doLuceneKeywordSearch(List<String> keywords) throws IOException, ParseException {
    List<String> fieldList = new ArrayList<>();
    for (D2rMap map : maps) {

      String id = map.getId();
      SelectStatement query = map.getQuery();
      for (String column : query.getColumns()) {
        String col = D2rUtil.getFieldNameUpperCase(column);
        fieldList.add((id  + "_" + col).toUpperCase());
      }
    }

    String[] fields = new String[fieldList.size()];
    fieldList.toArray(fields);
    SearchResult result = doLuceneKeywordSearch(fields, keywords);

    return new DocToTripleStream(result, this);
  }

  /** Generated instances for all D2R maps. */
  public StreamCollection<Document> getAllAsLuceneDocs() throws D2RException {
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

    StreamCollection<Document> result = new StreamCollection();
    for (D2rMap map : maps) {
      result.add(createLuceneDocStreamFactory(map, dataSourceManager.getDataSource(), new ArrayList<>()));
    }

    return result;
  }


  /**
   * TODO
   * @param keywords
   * @return
   * @throws D2RException
   */
  public StreamCollection<Triple> doKeywordSearch(List<String> keywords) throws D2RException {

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

    StreamCollection<Triple> result = new StreamCollection<>();

    // Generate instances for all maps
    for (D2rMap map : maps) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns);
      query.addTemporaryCondition(keywordCondition);
      StreamFactory<Triple> stream = createTripleStreamFactory(map, dataSourceManager.getDataSource(), new ArrayList<>());
      result.add(stream);
    }

    return result;
  }

  /** Generated instances for all D2R maps. */
  public StreamCollection<Triple> getAllAsTriples() throws D2RException {
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

    StreamCollection<Triple> result = new StreamCollection<>();
    for (D2rMap map : maps)
      result.add(createTripleStreamFactory(map, dataSourceManager.getDataSource(), new ArrayList<>()));

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

  public D2rMap getMapById(String id) {
    return idToMap.get(id);
  }

  public URINormalizer getNormalizer() {
    return normalizer;
  }

  public void reindex() throws D2RException {
    StreamCollection<Document> docStream = getAllAsLuceneDocs();

    try {
      index.open();
      docStream.start();
      index.reindex(docStream);
      // for (Document doc : docStream) {System.out.println(doc.toString());}


    } catch (IOException e) {
      throw new D2RException("Error while reindexing", e);
    } finally {
      FileUtil.closeSilently(docStream, true);
    }

  }

  public void shutdown() {
    FileUtil.closeSilently(index, true);
  }


  private void clear() throws FactoryException {
    // clear maps
    for (D2rMap map : maps)
      map.clear();
  }

  private SearchResult doLuceneKeywordSearch(String[] fieldNameArray , List<String> keywords) throws IOException, ParseException {
    Analyzer analyzer = new StandardAnalyzer();
    QueryParser parser = new MultiFieldQueryParser(fieldNameArray,analyzer);

    StringBuilder keywordsConcat = new StringBuilder();
    for (String keyword : keywords) {
      keywordsConcat.append(keyword);
      keywordsConcat.append(" ");
    }

    /*
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (String keyword : keywords) {
      Query query = parser.parse(keyword);
      if (keyword.startsWith("+"))
        builder.add(query, BooleanClause.Occur.MUST);
      else
        builder.add(query, BooleanClause.Occur.SHOULD);
    }

    Query query = builder.build();
    */

    Query query = parser.parse(keywordsConcat.toString());

    if (log.isDebugEnabled())
      log.debug("Constructed query: " + query);


    return new SearchResult(index.createReader(), query);
  }
}