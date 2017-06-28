package de.unipassau.medsapce.rdf;

import de.fuberlin.wiwiss.d2r.D2RMap;
import de.fuberlin.wiwiss.d2r.URINormalizer;
import de.unipassau.medsapce.SQL.SQLQueryResultStream;
import de.unipassau.medsapce.SQL.SQLResultTuple;
import de.unipassau.medspace.util.SqlUtil;
import org.apache.jena.graph.Triple;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class MappedTripleStream implements TripleStream {

  private SQLQueryResultStream queryStream;
  private D2RMap map;
  private LinkedList<Triple> tripleCache;
  private URINormalizer normalizer;
  private volatile boolean isClosed;

  public MappedTripleStream(DataSource dataSource, String query, int maxRowSie, int fetchSize, D2RMap map, URINormalizer normalizer) throws SQLException {
    this.queryStream = SqlUtil.executeQuery(dataSource, query, maxRowSie, fetchSize);
    this.map = map;
    this.normalizer = normalizer;
    tripleCache = new LinkedList<>();
    isClosed = false;
  }

  @Override
  public void close() throws IOException {
    tripleCache.clear();
    isClosed = true;
    queryStream.close();
  }

  @Override
  public Iterator<Triple> iterator() {
    return this;
  }

  @Override
  public void forEach(Consumer<? super Triple> action) {
    forEachRemaining(action);
  }


  /**
   * Not implemented
   * @throws UnsupportedOperationException
   */
  @Override
  public Spliterator<Triple> spliterator() {
    throw new UnsupportedOperationException("spliterator() is not implemented!");
  }

  @Override
  public boolean hasNext() {
    if (isClosed) throw new IllegalStateException("MappedTripleStream is already closed!");
    if (!tripleCache.isEmpty()) return true;
    return queryStream.iterator().hasNext();
  }

  @Override
  public Triple next() {
    if (isClosed) throw new IllegalStateException("MappedTripleStream is already closed!");
    if (tripleCache.isEmpty()) {
      SQLResultTuple tuple = queryStream.iterator().next();
      List<Triple> tupleTriples = map.createTriples(tuple, normalizer);
      tripleCache.addAll(tupleTriples);
    }
    return tripleCache.removeFirst();
  }

  /**
   * Not implemented
   * @throws UnsupportedOperationException
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove() is not implemented!");
  }

  @Override
  public void forEachRemaining(Consumer<? super Triple> action) {
    action.accept(next());
  }
}
