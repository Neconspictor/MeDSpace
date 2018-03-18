package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;

import de.unipassau.medspace.common.rdf.mapping.ClassMapping;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.util.StringUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.DocumentAdapter;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.Identifiable;
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
public abstract class LucenePdfFileDocAdapter<ClassType extends Identifiable>
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
  protected static final String META_DATA_TAGS = "META_DATA_TAGS";

  /**
   * TODO
   */
  protected static final String OBJECT_ID_META_DATA_TAGS = "OBJECT_ID_META_DATA_TAGS";

  /**
   * TODO
   */
  protected List<Pair<String, PropertyMapping>> fieldNamePropertyPairs;

  /**
   * TODO
   */
  protected List<String> metaDataFields;

  /**
   * TODO
   */
  protected List<String> notExportedSearchableFields;

  /**
   * TODO
   */
  protected final ClassMapping classParsing;

  /**
   * TODO
   */
  protected LucenePdfFileDocAdapter(ClassMapping classParsing) {
    this.fieldNamePropertyPairs = new ArrayList<>();
    this.notExportedSearchableFields = new ArrayList<>();
    this.classParsing = classParsing;
    this.metaDataFields = new ArrayList<>();

    metaDataFields.add(META_DATA_TAGS);
    metaDataFields.add(OBJECT_ID_META_DATA_TAGS);

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

    //add meta data tags
    String tags = createListString(classParsing.getMetaData());
    document.add(createField(META_DATA_TAGS, tags));

    // add content of the image folder structure as searchable meta-data
    // we use not File.separaotor as for URIs '/' is always used!
    List<String> tokens = StringUtil.tokenize(source.getId(), "/");
    String concatenated = StringUtil.concat(tokens, " ");
    document.add(createField(OBJECT_ID_META_DATA_TAGS, concatenated));


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
  public List<Pair<String, PropertyMapping>>getFieldNamePropertyPairs() {
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
  protected void addPair(String fieldName, PropertyMapping property) {
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
    return new TextField(fieldName, RdfUtil.format(value), Field.Store.YES);
  }

  protected String createListString(List<String> list) {
    StringBuilder builder = new StringBuilder();
    for (String elem : list)
      builder.append(elem + " ");
    return builder.toString().trim();
  }

  /**
   * TODO
   * @param pair
   * @param field
   * @return
   */
  public abstract String createValue(Pair<String, PropertyMapping> pair, IndexableField field);

  /**
   * TODO
   */
  public List<String> getMetaDataFields() {
    return metaDataFields;
  }
}