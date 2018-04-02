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
 * A lucene class adapter that is used for mapping identifiables to lucene documents.
 */
public abstract class LuceneClassAdapter<ClassType extends Identifiable>
    implements DocumentAdapter<ClassType, Document, IndexableField> {

  /**
   * Specifies a lucene field that is used to store the class id of the class mapping.
   */
  public static final String CLASS_ID = "CLASS_ID";

  /**
   * Specifies a lucene field that is used to store meta data tags specified in the configuration file.
   * This type of meta data is the same for all objects assigned to the same class mapping.
   */
  public static final String META_DATA_TAGS = "META_DATA_TAGS";

  /**
   * Specifies a lucene field that is used to store the id of the identifiable.
   */
  public static final String OBJECT_ID = "ID";

  /**
   * Specifies a lucene field that is used to store meta data tags that are unique for the identifiable.
   */
  public static final String OBJECT_ID_META_DATA_TAGS = "OBJECT_ID_META_DATA_TAGS";


  /**
   * A decorator that is able to customize the mapping (e.g. by adding more content to index).
   */
  private final LuceneClassAdapter<? super ClassType> decorator;

  /**
   * The list of field name to property mapping.
   */
  protected final List<Pair<String, PropertyMapping>> fieldNamePropertyPairs;

  /**
   * Additional fields that should be used for storing meta data.
   */
  protected final List<String> metaDataFields;

  /**
   * The class mapping assigns the lucene document a certain (rdf) class type.
   */
  protected final ClassMapping classMapping;


  /**
   * Creates a new LuceneClassAdapter.
   * @param decorator Another LuceneClassAdapter that should be used as a decorator.
   */
  public LuceneClassAdapter(LuceneClassAdapter<? super ClassType> decorator) {
    this(null, decorator);
  }


  /**
   * Creates a new LuceneClassAdapter from a given class mapping and a decorator.
   *
   * @param decorator Another LuceneClassAdapter that should be used as a decorator.
   * @param classMapping The class mapping that specifies the RDF type for the lucene documents.
   * @throws IllegalArgumentException If the decorator uses another class mapping than the one that
   * was specified to use for this object.
   */
  public LuceneClassAdapter(ClassMapping classMapping, LuceneClassAdapter<? super ClassType> decorator)
  throws IllegalArgumentException {
    this.decorator = decorator;


    // All stacked decorators should use the same classMapping field
    if (decorator == null) {
      this.classMapping = classMapping;
    } else {

      // we actually don't want, that a classMapping is overwritten by an existing one
      // if they aren't equal (-> Bug in user code)!
      if (classMapping != null) {
        if (!decorator.classMapping.equals(classMapping)) {
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
   * Adds lucene fields to a lucene document. The field content is created by the source argument.
   * @param source The source to create the fields from.
   * @param doc The document to add the created fields to.
   * @throws IOException If any io error occurs.
   */
  protected abstract void addFields(ClassType source, Document doc) throws IOException;


  /**
   * Adds a mapping of a field name to a property mapping to this object.
   * @param fieldName The field name.
   * @param property The property mapping.
   */
  protected void addPair(String fieldName, PropertyMapping property) {
    fieldNamePropertyPairs.add(new Pair<>(fieldName, property));
  }

  /**
   * Cretaes a string value from a lucene field and a pair of a field name and a property mapping.
   * The created value can be resolved by this object or by its decorator.
   *
   * NOTE: This object has a higher priority. Thus if this object and its decorator could provide a value for the field,
   * the decorator will be ignored.
   * @param pair a pair of a field name and a property mapping.
   * @param field a lucene field
   * @return The value for the lucene
   * field.
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
   * Factory method for creating a lucene field out of a field name and a text value.
   * @param fieldName a lucene field
   * @param value a text value
   * @return A lucene field.
   */
  protected IndexableField createField(String fieldName, String value) {
    return new TextField(fieldName, value, Field.Store.YES);
  }

  /**
   * Factory method for creating a lucene field out of a field name and a integer value.
   * @param fieldName a lucene field
   * @param value a integer value
   * @return a lucene field.
   */
  protected IndexableField createField(String fieldName, int value) {
    return new TextField(fieldName, String.valueOf(value), Field.Store.YES);
  }

  /**
   * Factory method for creating a lucene field out of a field name and a date.
   * @param fieldName a lucene field
   * @param value a date
   * @return a lucene field.
   */
  protected IndexableField createField(String fieldName, Date value) {
    //return new TextField(fieldName, Util.format(value), Field.Store.YES);
    return new StringField(fieldName, RdfUtil.format(value), Field.Store.YES);
  }

  /**
   * Concates a list of strings to one string. The elements of the list will be separated by spaces.
   * @param list The list of strings.
   * @return The concatenated list elements separated by spaces.
   */
  protected String createListString(List<String> list) {
    StringBuilder builder = new StringBuilder();
    for (String elem : list)
      builder.append(elem + " ");
    return builder.toString().trim();
  }

  /**
   * Decorates a given document using a source object.
   * @param source The source object
   * @param doc a lucene document.
   * @throws IOException If an io error occurs.
   */
  protected final void decorate(ClassType source, Document doc) throws IOException {
    if (decorator != null) {
      decorator.decorate(source, doc);
    }

    addFields(source, doc);
  }

  /**
   * Provides the content of a field using a pair of a field name and a property mapping.
   * This method is different from createDecoratedValue as it mustn't consult the decorator.
   * @param pair The pair of a field name and a property mapping
   * @param field The lucee field
   * @return The content of the field
   */
  protected abstract String getValue(Pair<String, PropertyMapping> pair, IndexableField field);
}