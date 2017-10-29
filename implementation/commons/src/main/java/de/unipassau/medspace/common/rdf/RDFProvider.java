package de.unipassau.medspace.common.rdf;

import java.util.List;

/**
 * Created by David Goeth on 29.10.2017.
 */
public interface RDFProvider {

  String getDefaultMimeType(String format);

  List<String> getFileExtensions(String format);

  TripleWriterFactory getWriterFactory();

  boolean isValid(String format);
}