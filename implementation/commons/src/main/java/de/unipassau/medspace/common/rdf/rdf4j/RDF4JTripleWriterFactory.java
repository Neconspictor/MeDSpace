package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.rdf.TripleWriter;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by David Goeth on 29.10.2017.
 */
public class RDF4JTripleWriterFactory implements TripleWriterFactory {
  @Override
  public TripleWriter create(OutputStream out, String format) throws IOException, NotValidArgumentException {
    RDFFormat language = RDF4JLanguageFormats.getFormatFromString(format);
    return new RDF4JTripleWriter(out, language);
  }
}