package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.common.util.SqlUtil;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.stream.SqlToTripleStream;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public class D2rKeywordSearcher implements KeywordSearcher<Triple> {

  private static Logger log = Logger.getLogger(D2rKeywordSearcher.class);

  private KeywordSearcher<Document> keywordSearcher;
  private boolean useLucene;
  private D2rProxy proxy;

  public D2rKeywordSearcher(D2rProxy proxy, KeywordSearcher<Document> keywordSearcher) throws IOException {
    this.keywordSearcher = keywordSearcher;
    this.proxy = proxy;
    useLucene = true;
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
    if (useLucene) {
      DataSourceStream<Document> result =  keywordSearcher.searchForKeywords(keywords);
      return new DocToTripleStream(result, proxy.getSqlResultFactory());
    }
    return searchByDatasource(keywords);
  }

  private DataSourceStream<Triple> searchByDatasource(List<String> keywords) throws IOException {

    StreamCollection<Triple> result = new StreamCollection<>();

    // Generate instances for all maps
    for (D2rMap map : proxy.getMaps()) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns);
      query.addTemporaryCondition(keywordCondition);
      DataSourceManager manager = proxy.getDataSourceManager();
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

    //generate resources using the Connection
    return () -> {
      SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
      return new SqlToTripleStream(queryParams, map);
    };
  }
}