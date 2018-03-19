package de.unipassau.medspace.common.rdf.converter;

import java.util.List;

/**
 * TODO
 */
public interface DocFieldService<DocType, FieldType> {

  /**
   * TODO
   * @param document
   * @return
   */
  List<FieldType> getFields(DocType document);

  /**
   * TODO
   * @param field
   * @return
   */
  String getStringValue(FieldType field);
}
