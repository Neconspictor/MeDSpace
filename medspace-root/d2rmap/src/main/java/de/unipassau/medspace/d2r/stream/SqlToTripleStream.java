package de.unipassau.medspace.d2r.stream;

import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.d2r.D2rMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Creates a stream that converts a stream of sql tuples to triple stream.
 */
public class SqlToTripleStream extends TripleCacheStream<SQLResultTuple> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(SqlToTripleStream.class);

  /**
   * Query parameters used co create a new sql stream.
   */
  private SqlStream.QueryParams startParams;

  /**
   * The D2rMap the resulting sql tuples can be mapped to.
   */
  private D2rMap map;

  /**
   * Creates a new SqlToTripleStream
   * @param queryParams Query parameters used co create a new sql stream.
   * @param map The D2rMap the resulting sql tuples can be mapped to.
   * @throws IOException If an IO-Error occurs.
   */
  public SqlToTripleStream(SqlStream.QueryParams queryParams, D2rMap map) throws IOException {
    super();

    assert  queryParams != null;
    assert map != null;

    startParams =queryParams;
    this.map = map;

    try {
      stream = new SqlStream(startParams);
    } catch (SQLException e) {
      throw new IOException("Couldn't createDoc stream to the sql datasource", e);
    }
  }

  @Override
  public void close() throws IOException {
    super.close();
    if (log.isDebugEnabled())
      log.debug("Successfully closed.");
  }

  @Override
  protected List<Triple> createTriples(SQLResultTuple elem) throws IOException {
    return map.createTriples(elem);
  }
}