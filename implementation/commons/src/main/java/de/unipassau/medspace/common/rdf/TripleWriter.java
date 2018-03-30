package de.unipassau.medspace.common.rdf;

import java.io.Closeable;
import java.io.IOException;

/**
 * An Triple writer is used for writing RDF triples to a destination.
 */
public interface TripleWriter extends Closeable {

  /**
   * Checks if this writer is closed.
   * @return
   */
  boolean isClosed();

  /**
   * Writes an RDF triple.
   * @param triple The triple to write.
   * @throws IOException If any IO error occurs.
   */
  void write(Triple triple) throws IOException;

  /**
   * Writes a namespace.
   * @param prefix The prefix of the namespace.
   * @param iri The IRI of the namespace
   * @throws IOException If any IO error occurs.
   */
  void write(String prefix, String iri) throws IOException;
}