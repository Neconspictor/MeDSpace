package de.unipassau.medspace.d2r;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.SQL.SelectStatement;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.lucene.document.Document;

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
  private DataSourceManager dataSourceManager;


  public D2rProxy(DataSourceManager dataSourceManager) throws D2RException {
    assert dataSourceManager != null;

    this.dataSourceManager = dataSourceManager;
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
    for (String condition : conditionList) {
      statement.addTemporaryCondition(condition);
    }

    String query = statement.toString();

    SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
    StreamFactory<SQLResultTuple> resultTupleFactory = () -> {
      try {
        return new SqlStream(queryParams);
      } catch (SQLException e) {
        throw new IOException("Couldn't createDoc stream to the sql datasource", e);
      }
    };

    return () -> new DataSourceStream<MappedSqlTuple>() {
      private DataSourceStream<SQLResultTuple> source = resultTupleFactory.create();
      @Override
      public void close() throws IOException {
        source.close();
      }

      @Override
      public boolean hasNext() {
        return source.hasNext();
      }

      @Override
      public MappedSqlTuple next() {
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
  public DataSourceStream<MappedSqlTuple> getAllData(List<D2rMap> maps) throws IOException {
    StreamCollection<MappedSqlTuple> result = new StreamCollection<>();
    for (D2rMap map : maps) {
      result.add(createStreamFactory(map, dataSourceManager.getDataSource(), new ArrayList<>()));
    }
    result.start();
    return result;
  }
}