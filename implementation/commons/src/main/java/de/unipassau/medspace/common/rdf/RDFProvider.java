package de.unipassau.medspace.common.rdf;

import java.util.List;
import java.util.Set;

/**
 * An RDF provider is used for interaction with all RDF related things.
 */
public interface RDFProvider {

  /**
   * Provides the default mime type for an RDF language format.
   * @param format The language format.
   * @return The default mime type.
   */
  String getDefaultMimeType(String format);

  /**
   * Provides a list of file extensions for a given RDF language format.
   * @param format The language format.
   * @return a list of file extensions
   */
  List<String> getFileExtensions(String format);

  /**
   * Provides an RDF factory.
   * @return an RDF factory.
   */
  RDFFactory getFactory();

  /**
   * Provides a factory for triple writers.
   * @return a factory for triple writers.
   */
  TripleWriterFactory getWriterFactory();

  /**
   * Checks if a given RDF language format is supported.
   * @param format The RDF language format
   * @return true if a given RDF language format is supported.
   */
  boolean isValid(String format);

  /**
   * Provides a set of supported language formats.
   * @return a set of supported language formats.
   */
  Set<String> getSupportedFormats();

  /**
   * Provides a formatted string that lists all supported RDF language formats.
   * This string can be used for printing.
   * @return a formatted string that lists all supported RDF language formats.
   */
  String getSupportedFormatsPrettyPrint();
}