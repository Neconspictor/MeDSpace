package de.unipassau.medspace.d2r.stream;

import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.d2r.D2rMap;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class SqlToTripleStream extends TripleCacheStream<SQLResultTuple> {

  private static Logger log = Logger.getLogger(SqlToTripleStream.class);
  private SqlStream.QueryParams startParams;
  private D2rMap map;

  public SqlToTripleStream(SqlStream.QueryParams queryParams, D2rMap map) throws IOException {
    super();

    assert  queryParams != null;
    assert map != null;

    startParams =queryParams;
    this.map = map;

    try {
      stream = new SqlStream(startParams);
    } catch (SQLException e) {
      throw new IOException("Couldn't create stream to the sql datasource", e);
    }
  }

  @Override
  public void close() throws IOException {
    super.close();
    log.debug("Successfully closed.");
  }

  @Override
  protected List<Triple> createTriples(SQLResultTuple elem) {
    return map.createTriples(elem);
  }
}