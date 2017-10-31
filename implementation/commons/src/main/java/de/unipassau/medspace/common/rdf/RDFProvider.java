package de.unipassau.medspace.common.rdf;

import java.util.List;
import java.util.Set;

/**
 * Created by David Goeth on 29.10.2017.
 */
public interface RDFProvider {

  String getDefaultMimeType(String format);

  List<String> getFileExtensions(String format);

  RDFFactory getFactory();

  TripleWriterFactory getWriterFactory();

  boolean isValid(String format);

  Set<String> getSupportedFormats();

  String getSupportedFormatsPrettyPrint();
}