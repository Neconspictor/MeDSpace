package de.unipassau.medspace.rdf;

import de.fuberlin.wiwiss.d2r.D2rMap;
import de.fuberlin.wiwiss.d2r.URINormalizer;
import de.unipassau.medspace.SQL.SQLQueryResultStream;
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
public class D2rMapTripleStream extends  AbstractTripleStream {

  private static Logger log = Logger.getLogger(D2rMapTripleStream.class);

  private SQLQueryResultStream queryStream;
  private SQLQueryResultStream.QueryParams startParams;
  private D2rMap map;
  private Queue<Triple> tripleCache;
  private URINormalizer normalizer;

  public D2rMapTripleStream(SQLQueryResultStream.QueryParams queryParams, D2rMap map, URINormalizer normalizer) {
    super();

    assert  queryParams != null;
    assert map != null;
    assert normalizer != null;

    queryStream = null;
    startParams =queryParams;
    this.map = map;
    this.normalizer = normalizer;
    tripleCache = new LinkedList<>();
  }

  @Override
  public void close() throws IOException {
    if (!started) throw new IllegalStateException("D2rMapTripleStream hasn't started yet!");
    if (isClosed) throw new IllegalStateException("D2rMapTripleStream is already closed!");
    isClosed = true;
    tripleCache.clear();
    queryStream.close();
  }

  @Override
  public boolean hasNext() {
    if (!started) throw new IllegalStateException("D2rMapTripleStream is closed!");
    if (!tripleCache.isEmpty()) return true;
    return queryStream.iterator().hasNext();
  }

  @Override
  public Triple next() {
    if (!started) throw new IllegalStateException("D2rMapTripleStream is closed!");
    if (tripleCache.isEmpty()) {
      SQLResultTuple tuple = queryStream.iterator().next();
      List<Triple> tupleTriples = map.createTriples(tuple, normalizer);
      tripleCache.addAll(tupleTriples);
    }
    return tripleCache.poll();
  }

  @Override
  public void start() throws IOException {
    if (isClosed) throw new IllegalStateException("D2rMapTripleStream is already closed!");
    if(started) throw new IllegalStateException("D2rMapTripleStream has already started!");
    started = true;
    try {
      queryStream = new SQLQueryResultStream(startParams);
    } catch (SQLException e) {
      throw new IOException("Couldn't start SQLQueryResultStream", e);
    }
  }
}