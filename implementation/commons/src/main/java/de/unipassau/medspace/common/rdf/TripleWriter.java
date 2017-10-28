package de.unipassau.medspace.common.rdf;

import java.io.IOException;

/**
 * Created by David Goeth on 28.10.2017.
 */
public interface TripleWriter {

  void write(Triple triple) throws IOException;
}