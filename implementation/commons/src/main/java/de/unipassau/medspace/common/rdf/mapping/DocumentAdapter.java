package de.unipassau.medspace.common.rdf.mapping;

import de.unipassau.medspace.common.util.Converter;
import org.javatuples.Pair;

import java.util.List;

/**
 * TODO
 */
public interface DocumentAdapter<ClassType extends Identifiable, DocType, FieldType>
    extends Converter<ClassType, DocType> {


  /**
   * TODO
   * @param pair
   * @param field
   * @return
   */
  String createValue(Pair<String, PropertyMapping> pair, FieldType field);

  /**
   * TODO
   * @return
   */
  String getClassBaseURI();

  /**
   * TODO
   * @return
   */
  List<Pair<String, PropertyMapping>> getFieldNamePropertyPairs();


  List<String> getMetaDataFields();

  /**
   * TODO
   * @param document
   * @return
   */

  String getObjectId(DocType document);

  /**
   * TODO
   * @param document
   * @return
   */
  boolean isConvertible(DocType document);

}
