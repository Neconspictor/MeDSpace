package de.unipassau.medspace.rdf;

import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by David Goeth on 28.06.2017.
 */
public interface TripleStream extends Closeable, Iterable<Triple>, Iterator<Triple> {

  /**
   * Checks if this TripleStream is open. The stream is open, if it has started streaming and isn't closed.
   * @return true if this TripleStream is open;
   */
  boolean isOpen();

  /**
   * Starts this triple stream
   */
  void start() throws IOException;
}
