package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;

import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.ClassParsing;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.PropertyParsing;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.DocumentAdapter;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.Identifiable;
import de.unipassau.medspace.wrapper.pdf_wrapper.rdf_mapping.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * TODO
 */
public abstract class LuceneDocAdapter<ClassType extends Identifiable>
    implements DocumentAdapter<ClassType, Document> {


  /**
   * TODO
   */
  public static final String CLASS_ID = "CLASS_ID";

  /**
   * TODO
   */
  public static final String OBJECT_ID = "ID";

  /**
   * TODO
   */
  protected List<Pair<String, PropertyParsing>> fieldNamePropertyPairs;

  /**
   * TODO
   */
  protected List<String> notExportedSearchableFields;

  /**
   * TODO
   */
  protected final ClassParsing classParsing;

  /**
   * TODO
   */
  protected LuceneDocAdapter(ClassParsing classParsing) {
    this.fieldNamePropertyPairs = new ArrayList<>();
    this.notExportedSearchableFields = new ArrayList<>();
    this.classParsing = classParsing;

  }


  /**
   * TODO
   * @param source The object to convert
   * @return
   * @throws IOException
   */
  @Override
  public final Document convert(ClassType source) throws IOException {
    Document document = new Document();
    //assign it the id
    document.add(new StringField(CLASS_ID, classParsing.getClassId(), Field.Store.YES));
    document.add(new StringField(OBJECT_ID, source.getId(), Field.Store.YES));
    addFields(source, document);
    return document;
  }

  /**
   * TODO
   * @param document
   * @return
   */
  public boolean isConvertible(Document document) {
    IndexableField field = document.getField(CLASS_ID);
    if (field == null) return false;
    return field.stringValue().equals(classParsing.getClassId());
  }


  /**
   * TODO
   * @param document
   * @return
   */
  public String getObjectId(Document document) {
    return document.get(OBJECT_ID);
  }

  /**
   * TODO
   * @return
   */
  public List<Pair<String, PropertyParsing>>getFieldNamePropertyPairs() {
    return Collections.unmodifiableList(fieldNamePropertyPairs);
  }

  public List<String> getNotExportedSearchableFields() {
    return Collections.unmodifiableList(notExportedSearchableFields);
  }

  /**
   * TODO
   * @return
   */
  public String getClassBaseURI() {
    return classParsing.getRdfType();
  }

  /**
   * TODO
   * @param source
   * @param doc
   * @throws IOException
   */
  protected abstract void addFields(ClassType source, Document doc) throws IOException;


  /**
   * TODO
   * @param fieldName
   */
  protected void addNotExportableSearchableField(String fieldName) {
    notExportedSearchableFields.add(fieldName);
  }

  /**
   * TODO
   * @param fieldName
   * @param property
   */
  protected void addPair(String fieldName, PropertyParsing property) {
    fieldNamePropertyPairs.add(new Pair<>(fieldName, property));
  }


  /**
   * TODO
   * @param fieldName
   * @param value
   * @return
   */
  protected IndexableField createField(String fieldName, String value) {
    return new TextField(fieldName, value, Field.Store.YES);
  }

  /**
   * TODO
   * @param fieldName
   * @param value
   * @return
   */
  protected IndexableField createField(String fieldName, int value) {
    return new TextField(fieldName, String.valueOf(value), Field.Store.YES);
  }

  /**
   * TODO
   * @param fieldName
   * @param value
   * @return
   */
  protected IndexableField createField(String fieldName, Date value) {
    return new TextField(fieldName, Util.format(value), Field.Store.YES);
  }

  /**
   * TODO
   * @param pair
   * @param field
   * @return
   */
  public abstract String createValue(Pair<String, PropertyParsing> pair, IndexableField field);
}