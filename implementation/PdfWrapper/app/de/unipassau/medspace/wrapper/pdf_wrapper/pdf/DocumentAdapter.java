package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.util.Converter;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.util.List;

/**
 * TODO
 */
public interface DocumentAdapter<ClassType extends Identifiable, DocType> extends Converter<ClassType, DocType> {


  /**
   * TODO
   * @param pair
   * @param field
   * @return
   */
  String createValue(Pair<String, PropertyMapping> pair, IndexableField field);

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