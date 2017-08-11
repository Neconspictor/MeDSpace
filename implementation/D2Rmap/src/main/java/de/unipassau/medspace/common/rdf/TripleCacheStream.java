package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.stream.Stream;
import org.apache.jena.graph.Triple;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by David Goeth on 03.07.2017.
 */
public abstract class TripleCacheStream<E> implements Stream<Triple> {

  protected Queue<Triple> tripleCache;
  protected Stream<E> stream;

  public TripleCacheStream(Stream<E> stream) {
    this();
    this.stream = stream;
  }

  protected TripleCacheStream() {
    tripleCache = new LinkedList<>();
    stream = null;
  }

  @Override
  public void close() throws IOException {
    tripleCache.clear();
    stream.close();
  }

  protected abstract List<Triple> createTriples(E elem);

  @Override
  public boolean hasNext() throws IOException {
    if (!tripleCache.isEmpty()) return true;
    return stream.hasNext();
  }

  @Override
  public Triple next() throws IOException {
    if (tripleCache.isEmpty()) {
      E nextElem = stream.next();
      tripleCache.addAll(createTriples(nextElem));
    }
    return tripleCache.poll();
  }
}