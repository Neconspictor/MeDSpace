package de.unipassau.medspace.d2r;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.SQL.SelectStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.sql.DataSource;

/**
 * D2R Proxy exports data from a RDBMS into an RDF model using the MeDSpace D2r Map language.
 * MeDSpace D2r Map is a declarative, XML-based language to describe mappings between the relational data and rdf.
 * The result can be serialized in varias rdf serializations.
 */
public class D2rProxy {
  /**
   * Logger instance of this class.
   * */
  private static Logger log = LoggerFactory.getLogger(D2rProxy.class);

  /**
   * The datasource manager is used to get a open connection to the datasource
   */
  private ConnectionPool connectionPool;


  /**
   * Creates a new D2rProxy.
   * @param connectionPool The connection pool to get connections to the datasource from.
   */
  public D2rProxy(ConnectionPool connectionPool) {
    assert connectionPool != null;
    this.connectionPool = connectionPool;
  }

  /**
   * Creates a factory , that is able to create a stream of sql tuples that have a reference to a D2rMap that is able to
   * create triples from the tuple. The sql tuples from the stream will come from a select query given from the stated
   * D2rMap.
   * @param map The D2rMap the sql result tuples should be assigned to.
   * @param dataSource The datasource to fetch data from.
   * @param conditionList An list of optional sql WHERE conditions. The list of conditions will be added to
   *                      the sql select query of the D2rMap.
   * @return A factory for a stream of mapped sql result tuples.
   */
  public StreamFactory<MappedSqlTuple> createStreamFactory(D2rMap map, DataSource dataSource,
                                                           List<String> conditionList) {

    SelectStatement statement = map.getQuery();
    String query = statement.getSqlQuery(conditionList);

    SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
    StreamFactory<SQLResultTuple> resultTupleFactory = () -> {
      try {
        return new SqlStream(queryParams);
      } catch (SQLException e) {
        throw new IOException("Couldn't createDoc stream to the sql datasource", e);
      }
    };

    return () -> new Stream<MappedSqlTuple>() {
      private Stream<SQLResultTuple> source = resultTupleFactory.create();
      @Override
      public void close() throws IOException {
        source.close();
      }

      @Override
      public boolean hasNext() throws IOException {
        return source.hasNext();
      }

      @Override
      public MappedSqlTuple next() throws IOException {
        return new MappedSqlTuple(source.next(), map);
      }
    };
  }


  /**
   * Provides a stream of mapped sql result tuples, that represents the whole data set of the datasource proxied by this
   * class.
   * @param maps The D2rMaps used to fetch the data from the datasource.
   * @return A stream of mapped sql result tuples, that represents all the data the datasource can offer for the
   * specified D2rMap list.
   * @throws IOException If an IO-Error occurs.
   */
  public Stream<MappedSqlTuple> getAllData(List<D2rMap> maps) throws IOException {
    StreamCollection<MappedSqlTuple> result = new StreamCollection<>();
    for (D2rMap map : maps) {
      result.add(createStreamFactory(map, connectionPool.getDataSource(), new ArrayList<>()));
    }
    result.setRethrowExceptions(true);
    result.start();
    return result;
  }
}