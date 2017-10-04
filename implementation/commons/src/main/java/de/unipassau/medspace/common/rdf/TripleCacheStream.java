package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.stream.Stream;
import org.apache.jena.graph.Triple;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A TripleCacheStream wraps a another stream and converts the elements of the other stream to triples.
 */
public abstract class TripleCacheStream<E> implements Stream<Triple> {

  /**
   * Stores triples of a converted element from the source stream.
   */
  protected Queue<Triple> tripleCache;

  /**
   * The stream wrapped by this class.
   */
  protected Stream<E> stream;

  /**
   * Creates a new TripleCacheStream from another stream.
   * @param stream The stream that should be triplized by this class.
   */
  public TripleCacheStream(Stream<E> stream) {
    this();
    this.stream = stream;
  }

  /**
   * Default constructor. Protected as it shouldn't be used by clients.
   */
  protected TripleCacheStream() {
    tripleCache = new LinkedList<>();
    stream = null;
  }

  @Override
  public void close() throws IOException {
    tripleCache.clear();
    stream.close();
  }

  /**
   * Converts an object to a list of triples.
   * @param elem The object to convert.
   * @return A list of triples representing the converted object.
   */
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