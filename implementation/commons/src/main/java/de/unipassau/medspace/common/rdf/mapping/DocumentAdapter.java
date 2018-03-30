package de.unipassau.medspace.common.rdf.mapping;

import de.unipassau.medspace.common.util.Converter;
import org.javatuples.Pair;

import java.util.List;

/**
 * Represents an adapter for a document. This adapter is used to convert rdf data to documents and vice versa.
 */
public interface DocumentAdapter<ClassType extends Identifiable, DocType, FieldType>
    extends Converter<ClassType, DocType> {


  /**
   * Creates a value from a field and a pair of field name and a property mapping.
   * @param pair a pair of field name and a property mapping.
   * @param field The field.
   * @return The created value.
   */
  String createValue(Pair<String, PropertyMapping> pair, FieldType field);

  /**
   * Provides the base URI of the rdf class type.
   * @return
   */
  String getClassBaseURI();

  /**
   * Provides the field name peoperty pairs.
   * @return the field name peoperty pairs.
   */
  List<Pair<String, PropertyMapping>> getFieldNamePropertyPairs();


  /**
   * Provides a list of meta data fields.
   * @return a list of meta data fields.
   */
  List<String> getMetaDataFields();

  /**
   * Provides the object id of a document.
   * @param document the document.
   * @return the object id of a document.
   */

  String getObjectId(DocType document);

  /**
   * Checks if this adapter can convert a given document.
   * @param document the document.
   * @return true if this adapter can convert a given document.
   */
  boolean isConvertible(DocType document);

}