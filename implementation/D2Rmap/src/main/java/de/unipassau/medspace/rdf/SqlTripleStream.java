package de.unipassau.medspace.rdf;

import de.fuberlin.wiwiss.d2r.D2rMapper;
import de.unipassau.medspace.SQL.SqlStream;
import de.unipassau.medspace.common.URINormalizer;
import de.unipassau.medspace.SQL.SQLResultTuple;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class SqlTripleStream implements TripleStream {

  private static Logger log = Logger.getLogger(SqlTripleStream.class);

  private SqlStream queryStream;
  private SqlStream.QueryParams startParams;
  private D2rMapper map;
  private Queue<Triple> tripleCache;
  private URINormalizer normalizer;

  public SqlTripleStream(SqlStream.QueryParams queryParams, D2rMapper map, URINormalizer normalizer) throws IOException {
    super();

    assert  queryParams != null;
    assert map != null;
    assert normalizer != null;

    queryStream = null;
    startParams =queryParams;
    this.map = map;
    this.normalizer = normalizer;
    tripleCache = new LinkedList<>();

    try {
      queryStream = new SqlStream(startParams);
    } catch (SQLException e) {
      throw new IOException("Couldn't create stream to the sql datasource", e);
    }
  }

  @Override
  public void close() throws IOException {
    tripleCache.clear();
    queryStream.close();
    log.debug("Successfully closed.");
  }

  @Override
  public boolean hasNext() {
    if (!tripleCache.isEmpty()) return true;
    return queryStream.iterator().hasNext();
  }

  @Override
  public Triple next() {
    if (tripleCache.isEmpty()) {
      SQLResultTuple tuple = queryStream.iterator().next();
      List<Triple> tupleTriples = map.createTriples(tuple, normalizer);
      tripleCache.addAll(tupleTriples);
    }
    return tripleCache.poll();
  }
}