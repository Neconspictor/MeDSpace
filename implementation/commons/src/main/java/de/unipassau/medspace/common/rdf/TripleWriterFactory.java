package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.exception.NoValidArgumentException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a factory for creating a triple writer.
 */
public interface TripleWriterFactory {

  /**
   * Creates a new triple writer.
   * @param out The destination, i.e. where the writer should write triples.
   * @param format The RDF language format the writer should use.
   * @return A new triple writer.
   * @throws IOException If any IO error occurs.
   * @throws NoValidArgumentException If one of the arguments cannot be used to create a triple writer.
   */
  TripleWriter create(OutputStream out, String format) throws IOException, NoValidArgumentException;
}