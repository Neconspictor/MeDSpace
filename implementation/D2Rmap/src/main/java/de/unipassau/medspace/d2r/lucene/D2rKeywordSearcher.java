package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.common.util.SqlUtil;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.stream.SqlToTripleStream;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A keyword searcher for a D2rWrapper
 */
public class D2rKeywordSearcher implements KeywordSearcher<Triple> {

  private static Logger log = LoggerFactory.getLogger(D2rKeywordSearcher.class);

  private boolean useLucene;
  private D2rWrapper wrapper;
  private KeywordSearcher<Triple> keywordSearcher; //TODO should use the same document type

  public D2rKeywordSearcher(D2rWrapper wrapper, KeywordSearcher<Triple> keywordSearcher) throws IOException {
    this.keywordSearcher = keywordSearcher;
    this.wrapper = wrapper;
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
  public DataSourceStream<Triple> searchForKeywords(List<String> keywords) throws IOException,
      NotValidArgumentException {
    if (useLucene) {
      return keywordSearcher.searchForKeywords(keywords);
    }
    return searchByDatasource(keywords);
  }

  private DataSourceStream<Triple> searchByDatasource(List<String> keywords) throws IOException {

    StreamCollection<Triple> result = new StreamCollection<>();

    // Generate instances for all maps
    for (D2rMap map : wrapper.getMaps()) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns);
      query.addTemporaryCondition(keywordCondition);
      DataSourceManager manager = wrapper.getDataSourceManager();
      StreamFactory<Triple> stream = createTripleStreamFactory(map, manager.getDataSource(), new ArrayList<>());
      result.add(stream);
    }

    result.start();

    return result;
  }

  /**
   * Creates a rdf triple stream from the specified datasource. For the rdf sql to rdf
   * mapping the specified D2rMap is used. The triple stream will be resticted by the given conditionList
   * argument.<br>
   *
   * NOTE: The returned TripleStream won't be started, so no connection to the datasource will be established yet.
   * @param map The sql to rdf mapper
   * @param  dataSource The sql datasource.
   * @param conditionList The query that should be executed on the datasource
   * @return TODO
   * @throws IOException TODO
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