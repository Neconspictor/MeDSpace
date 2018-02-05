package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.rdf.RDFResource;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.util.List;

/**
 * TODO
 */
public interface DocumentAdapter<ClassType> {


  /**
   * TODO
   * @return
   */
  Converter<ClassType, Document> getClassToDocumentConverter();

  /**
   * TODO
   * @param document
   * @return
   */
  boolean isConvertable(Document document);

  /**
   * TODO
   * @param document
   * @return
   */
  List<IndexableField> getValidFields(Document document);


  /**
   * TODO
   * @param document
   * @return
   */
  String getClassId(Document document);

  /**
   * TODO
   * @param fieldName
   * @return
   */
  Property getPropertyByFieldName(String fieldName);

  /**
   * TODO
   * @return
   */
  String getClassBaseURI();
}