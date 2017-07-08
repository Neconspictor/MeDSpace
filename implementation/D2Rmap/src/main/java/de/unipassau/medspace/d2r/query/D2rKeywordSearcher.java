package de.unipassau.medspace.d2r.query;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.indexing.lucene.FullTextSearchIndexWrapperImpl;
import de.unipassau.medspace.common.indexing.lucene.SearchResult;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.common.util.SqlUtil;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rProcessor;
import de.unipassau.medspace.d2r.exception.FactoryException;
import de.unipassau.medspace.d2r.indexing.SqlMapFactory;
import de.unipassau.medspace.d2r.stream.DocToTripleStream;
import de.unipassau.medspace.d2r.stream.SqlToTripleStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public class D2rKeywordSearcher implements KeywordSearcher<Triple> {

  private static Logger log = Logger.getLogger(D2rKeywordSearcher.class);

  private boolean useLucene;
  private D2rProcessor processor;

  public D2rKeywordSearcher(D2rProcessor processor) {
    useLucene = true;
    this.processor = processor;
  }

  /**
   * Tells this searcher to use the index or querying the datasource directly.
   * @param use true if the index should be used, or false, if the datasource should
   *            be queryied directly.
   */
  public void useLucene(boolean use) {
    useLucene = use;
  }

  @Override
  public DataSourceStream<Triple> searchForKeywords(List<String> keywords) throws IOException {
    if (useLucene) return searchByIndex(keywords);
    return searchByDatasource(keywords);
  }

  private DataSourceStream<Triple> searchByDatasource(List<String> keywords) throws IOException {
    Model model = null;

    try {
      model = de.unipassau.medspace.d2r.factory.ModelFactory.getInstance().createDefaultModel();
    }
    catch (FactoryException e) {
      throw new IOException("Could not get default Model from the ModelFactory.", e);
    }


    // add namespaces
   /* for (Map.Entry<String, String> ent : processor.getNamespaces().entrySet()) {
      model.setNsPrefix(ent.getKey(), ent.getValue());
    }*/

    StreamCollection<Triple> result = new StreamCollection<>();

    // Generate instances for all maps
    for (D2rMap map : processor.getMaps()) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns);
      query.addTemporaryCondition(keywordCondition);
      DataSourceManager manager = processor.getDataSourceManager();
      StreamFactory<Triple> stream = createTripleStreamFactory(map, manager.getDataSource(), new ArrayList<>());
      result.add(stream);
    }

    result.start();

    return result;
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
                                                         List<String> conditionList) throws IOException {

    SelectStatement statement = map.getQuery();
    for (String condition : conditionList) {
      statement.addTemporaryCondition(condition);
    }

    String query = statement.toString();
    statement.reset();

    //generate resources using the Connection
    return () -> {
      SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
      return new SqlToTripleStream(queryParams, map, processor.getNormalizer());
    };
  }

  private DataSourceStream<Triple> searchByIndex(List<String> keywords) throws IOException {
    List<String> fieldList = new ArrayList<>();
    for (D2rMap map : processor.getMaps()) {
      List<String> mappedColumns = SqlMapFactory.getMappedColumns(map);
      fieldList.addAll(mappedColumns);
    }

    String[] fields = new String[fieldList.size()];
    fieldList.toArray(fields);
    SearchResult result = null;
    try {
      result = doLuceneKeywordSearch(fields, keywords);
    } catch (IOException | ParseException e) {
      throw new IOException("Exception while querying the index: ", e);
    }

    return new DocToTripleStream(result, processor);
  }

  private SearchResult doLuceneKeywordSearch(String[] fieldNameArray , List<String> keywords) throws IOException, ParseException {
    Analyzer analyzer = new StandardAnalyzer();
    QueryParser parser = new MultiFieldQueryParser(fieldNameArray,analyzer);

    StringBuilder keywordsConcat = new StringBuilder();
    for (String keyword : keywords) {
      keywordsConcat.append(keyword);
      keywordsConcat.append(" ");
    }

    Query query = parser.parse(keywordsConcat.toString());

    if (log.isDebugEnabled())
      log.debug("Constructed query: " + query);

    FullTextSearchIndexWrapperImpl index = (FullTextSearchIndexWrapperImpl) processor.getIndex();
    return new SearchResult(index.createReader(), query);
  }
}