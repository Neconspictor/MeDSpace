package de.unipassau.medspace.d2r;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.d2r.exception.D2RException;
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
   * Logger
   * */
  private static Logger log = LoggerFactory.getLogger(D2rProxy.class);

  /**
   * The datasource manager is used to get a open connection to the datasource
   */
  private ConnectionPool connectionPool;


  public D2rProxy(ConnectionPool connectionPool) throws D2RException {
    assert connectionPool != null;

    this.connectionPool = connectionPool;
  }

  /**
   * TODO
   * @param map TODO
   * @param dataSource TODO
   * @param conditionList TODO
   * @return TODO
   * @throws IOException TODO
   */
  public StreamFactory<MappedSqlTuple> createStreamFactory(D2rMap map, DataSource dataSource,
                                                           List<String> conditionList) throws IOException {

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
   * TODO
   * @param maps
   * @return
   * @throws IOException
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