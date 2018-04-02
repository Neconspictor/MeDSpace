package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.TripleWriter;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A triple writer factory for RDF4J.
 */
public class RDF4JTripleWriterFactory implements TripleWriterFactory {
  @Override
  public TripleWriter create(OutputStream out, String format) throws IOException, NoValidArgumentException {
    RDFFormat language = RDF4JLanguageFormats.getFormatFromString(format);
    return new RDF4JTripleWriter(out, language);
  }
}