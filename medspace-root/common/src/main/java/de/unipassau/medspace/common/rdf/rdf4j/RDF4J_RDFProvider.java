package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * An RDF provider for RDF4J.
 */
public class RDF4J_RDFProvider  implements RDFProvider {

  private static Logger log = LoggerFactory.getLogger(RDF4J_RDFProvider.class);

  private final TripleWriterFactory factory;

  private final RDFFactory primitiveValueFactory;

  /**
   * Creates a new RDF4J_RDFProvider object.
   */
  public RDF4J_RDFProvider() {
    factory = new RDF4JTripleWriterFactory();
    primitiveValueFactory = new RDF4J_Factory();
  }

  @Override
  public String getDefaultMimeType(String format) {
    RDFFormat rdf4jFormat = getFormat(format);
    SimpleValueFactory factory;

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
  public RDFFactory getFactory() {
    return primitiveValueFactory;
  }

  @Override
  public TripleWriterFactory getWriterFactory() {
    return factory;
  }

  @Override
  public boolean isValid(String format) {
    return RDF4JLanguageFormats.isValidFormat(format);
  }

  @Override
  public Set<String> getSupportedFormats() {
    return RDF4JLanguageFormats.mappedFormats.keySet();
  }

  @Override
  public String getSupportedFormatsPrettyPrint() {
    return RDF4JLanguageFormats.getFormatsPrettyPrint();
  }

  private static RDFFormat getFormat(String format) {
    RDFFormat rdf4jFormat;

    try {
      rdf4jFormat = RDF4JLanguageFormats.getFormatFromString(format);
    } catch (NoValidArgumentException e) {
      log.debug("Couldn't retrieve rdf format from string '" + format + "'");
      return null;
    }
    return rdf4jFormat;
  }
}