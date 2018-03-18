package de.unipassau.medspace.common.lucene.rdf;

import de.unipassau.medspace.common.rdf.mapping.ClassMapping;
import de.unipassau.medspace.common.rdf.mapping.DocumentAdapter;
import de.unipassau.medspace.common.rdf.mapping.Identifiable;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.util.StringUtil;
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
public abstract class LuceneClassAdapter<ClassType extends Identifiable>
    implements DocumentAdapter<ClassType, Document, IndexableField> {

  /**
   * TODO
   */
  public static final String CLASS_ID = "CLASS_ID";

  public static final String META_DATA_TAGS = "META_DATA_TAGS";

  /**
   * TODO
   */
  public static final String OBJECT_ID = "ID";

  public static final String OBJECT_ID_META_DATA_TAGS = "OBJECT_ID_META_DATA_TAGS";


  private final LuceneClassAdapter<? super ClassType> decorator;

  protected final List<Pair<String, PropertyMapping>> fieldNamePropertyPairs;

  protected final List<String> metaDataFields;

  protected final ClassMapping classMapping;


  /**
   * TODO
   * @param decorator
   */
  public LuceneClassAdapter(LuceneClassAdapter<? super ClassType> decorator) {
    this(null, decorator);
  }


  /**
   * TODO
   * @param decorator
   * @param classMapping
   */
  public LuceneClassAdapter(ClassMapping classMapping, LuceneClassAdapter<? super ClassType> decorator) {
    this.decorator = decorator;


    // All stacked decorators should use the same classMapping field
    if (decorator == null) {
      this.classMapping = classMapping;
    } else {

      // we actually don't want, that a classMapping is overwritten by an existing one
      // if they aren't equal (-> Bug in user code)!
      if (classMapping != null) {
        if (!decorator.classMapping.equals(decorator)) {
          throw new IllegalArgumentException("ClassMapping mismatch!");
        }
      }
      this.classMapping = decorator.classMapping;
    }

    // The same for fieldNamePropertyPairs
    if (decorator == null) {
      fieldNamePropertyPairs = new ArrayList<>();
    } else {
      fieldNamePropertyPairs = decorator.fieldNamePropertyPairs;
    }

    // The same for metaDataFields
    if (decorator == null) {
      metaDataFields = new ArrayList<>();
    } else {
      metaDataFields = decorator.metaDataFields;
    }

    assert fieldNamePropertyPairs != null;
    assert metaDataFields != null;
    assert classMapping != null;


    metaDataFields.add(META_DATA_TAGS);
    metaDataFields.add(OBJECT_ID_META_DATA_TAGS);
  }


  @Override
  public Document convert(ClassType source) throws IOException {
    Document document = new Document();

    //assign it the id
    document.add(new StringField(OBJECT_ID, source.getId(), Field.Store.YES));
    document.add(new StringField(CLASS_ID, classMapping.getClassId(), Field.Store.YES));

    //add meta data tags
    String tags = createListString(classMapping.getMetaData());
    document.add(createField(META_DATA_TAGS, tags));


    // add content of the image folder structure as searchable meta-data
    // we use not File.separaotor as for URIs '/' is always used!
    List<String> tokens = StringUtil.tokenize(source.getId(), "/");
    String concatenated = StringUtil.concat(tokens, " ");
    document.add(createField(OBJECT_ID_META_DATA_TAGS, concatenated));

    // decorate the document
    decorate(source, document);

    return document;
  }

  @Override
  public final String createValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    return createDecoratedValue(pair, field);
  }

  @Override
  public String getClassBaseURI() {
    return classMapping.getRdfType();
  }


  @Override
  public List<Pair<String, PropertyMapping>>getFieldNamePropertyPairs() {
    return Collections.unmodifiableList(fieldNamePropertyPairs);
  }
  @Override
  public List<String> getMetaDataFields() {
    return Collections.unmodifiableList(metaDataFields);
  }

  @Override
  public String getObjectId(Document document) {
    return document.get(OBJECT_ID);
  }

  @Override
  public boolean isConvertible(Document document) {
    IndexableField field = document.getField(CLASS_ID);
    if (field == null) return false;
    return field.stringValue().equals(classMapping.getClassId());
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
   * @param property
   */
  protected void addPair(String fieldName, PropertyMapping property) {
    fieldNamePropertyPairs.add(new Pair<>(fieldName, property));
  }

  /**
   * TODO
   * @param pair
   * @param field
   * @return
   */
  protected final String createDecoratedValue (Pair<String, PropertyMapping> pair, IndexableField field) {
    String value = getValue(pair, field);
    if (value == null && decorator != null) {
      value = decorator.createDecoratedValue(pair, field);
    }

    // No decorator resolved the value? => default value
    if (value == null) {
      value = field.stringValue();
    }

    return value;
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
    //return new TextField(fieldName, Util.format(value), Field.Store.YES);
    return new StringField(fieldName, RdfUtil.format(value), Field.Store.YES);
  }

  /**
   * TODO
   * @param list
   * @return
   */
  protected String createListString(List<String> list) {
    StringBuilder builder = new StringBuilder();
    for (String elem : list)
      builder.append(elem + " ");
    return builder.toString().trim();
  }

  /**
   * TODO
   * @param source
   * @param doc
   * @throws IOException
   */
  protected final void decorate(ClassType source, Document doc) throws IOException {
    if (decorator != null) {
      decorator.decorate(source, doc);
    }

    addFields(source, doc);
  }

  protected abstract String getValue(Pair<String, PropertyMapping> pair, IndexableField field);
}