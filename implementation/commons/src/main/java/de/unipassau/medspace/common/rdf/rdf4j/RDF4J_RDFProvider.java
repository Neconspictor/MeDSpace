package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by David Goeth on 29.10.2017.
 */
public class RDF4J_RDFProvider  implements RDFProvider {

  private static Logger log = LoggerFactory.getLogger(RDF4J_RDFProvider.class);

  private final TripleWriterFactory factory;

  public RDF4J_RDFProvider() {
    factory = new RDF4JTripleWriterFactory();
  }

  @Override
  public String getDefaultMimeType(String format) {
    RDFFormat rdf4jFormat = getFormat(format);

    if (rdf4jFormat == null) return null;
    return rdf4jFormat.getDefaultMIMEType();
  }

  @Override
  public List<String> getFileExtensions(String format) {
    RDFFormat rdf4jFormat = getFormat(format);

    if (rdf4jFormat == null) return null;

    return rdf4jFormat.getFileExtensions();
  }

  @Override
  public TripleWriterFactory getWriterFactory() {
    return factory;
  }

  @Override
  public boolean isValid(String format) {
    return RDF4JLanguageFormats.isValidFormat(format);
  }

  private static RDFFormat getFormat(String format) {
    RDFFormat rdf4jFormat;

    try {
      rdf4jFormat = RDF4JLanguageFormats.getFormatFromString(format);
    } catch (NotValidArgumentException e) {
      log.debug("Couldn't retrieve rdf format from string '" + format + "'");
      return null;
    }
    return rdf4jFormat;
  }
}