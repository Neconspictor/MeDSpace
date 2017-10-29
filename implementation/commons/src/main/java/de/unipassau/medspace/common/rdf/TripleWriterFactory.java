package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.exception.NotValidArgumentException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by David Goeth on 29.10.2017.
 */
public interface TripleWriterFactory {

  TripleWriter create(OutputStream out, String format) throws IOException, NotValidArgumentException;
}