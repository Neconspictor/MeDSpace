package de.unipassau.medspace.d2r.query;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.common.util.SqlUtil;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.stream.SqlToTripleStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A keyword searcher for a D2rWrapper
 */
public class D2rKeywordSearcher implements KeywordSearcher<Triple> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(D2rKeywordSearcher.class);

  /**
   * The D2rWrapper this class operates on.
   */
  private D2rWrapper<?> wrapper;

  /**
   * Creates a new D2rKeywordSearcher.
   * @param wrapper The D2rWrapper to search on.
   * @throws IOException If an IO-Error occurs.
   */
  public D2rKeywordSearcher(D2rWrapper wrapper) throws IOException {
    this.wrapper = wrapper;
  }

  @Override
  public Stream<Triple> searchForKeywords(List<String> keywords) throws IOException,
      NotValidArgumentException {
    StreamCollection<Triple> result = new StreamCollection<>();

    // Generate instances for all maps
    for (D2rMap map : wrapper.getMaps()) {

      SelectStatement query = map.getQuery();
      List<String> columns = query.getColumns();

      String keywordCondition = SqlUtil.createKeywordCondition(keywords, columns, SqlUtil.Operator.OR);

      ConnectionPool manager = wrapper.getConnectionPool();
      StreamFactory<Triple> stream = createTripleStreamFactory(map, manager.getDataSource(),
          Arrays.asList(keywordCondition));
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
    String query = statement.getSqlQuery(conditionList);

    //generate resources using the Connection
    return () -> {
      SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
      return new SqlToTripleStream(queryParams, map);
    };
  }
}