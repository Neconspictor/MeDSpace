package de.unipassau.medspace.common.rdf.converter;

import java.util.List;

/**
 * A service for retrieving fields and values from a document type.
 */
public interface DocFieldService<DocType, FieldType> {

  /**
   * Provides the list of fields from a document.
   * @param document the document.
   * @return the list of fields.
   */
  List<FieldType> getFields(DocType document);

  /**
   * Provides the value of a field.
   * @param field the field.
   * @return The value.
   */
  String getStringValue(FieldType field);
}
