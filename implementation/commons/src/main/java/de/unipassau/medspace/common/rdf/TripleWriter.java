package de.unipassau.medspace.common.rdf;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by David Goeth on 28.10.2017.
 */
public interface TripleWriter extends Closeable {

  void write(Triple triple) throws IOException;

  void write(String prefix, String iri) throws IOException;
}