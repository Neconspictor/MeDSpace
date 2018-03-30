package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.rdf.mapping.ClassMapping;
import de.unipassau.medspace.common.rdf.mapping.Identifiable;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.util.StringUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.DocumentAdapter;
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
 * A document adapter for PDF files.
 */
public abstract class LucenePdfFileDocAdapter<ClassType extends Identifiable>
    extends LuceneClassAdapter<ClassType> {


  /**
   * A list of fields that should be considered for searching but are not used for exporting RDF data.
   */
  protected List<String> notExportedSearchableFields;


  /**
   * Creates a new LucenePdfFileDocAdapter object.
   * @param classMapping The class mapping to use.
   */
  protected LucenePdfFileDocAdapter(ClassMapping classMapping) {
    super(classMapping, null);
    this.notExportedSearchableFields = new ArrayList<>();
  }


  @Override
  public final Document convert(ClassType source) throws IOException {
    Document document = new Document();
    //assign it the id
    document.add(new StringField(CLASS_ID, classMapping.getClassId(), Field.Store.YES));
    document.add(new StringField(OBJECT_ID, source.getId(), Field.Store.YES));
    addFields(source, document);

    //add meta data tags
    String tags = createListString(classMapping.getMetaData());
    document.add(createField(META_DATA_TAGS, tags));

    // add content of the image folder structure as searchable meta-data
    // we use not File.separaotor as for URIs '/' is always used!
    List<String> tokens = StringUtil.tokenize(source.getId(), "/");
    String concatenated = StringUtil.concat(tokens, " ");
    document.add(createField(OBJECT_ID_META_DATA_TAGS, concatenated));


    return document;
  }


  /**
   * Provides the list of not exported but searchable fields.
   * @return the list of not exported but searchable fields.
   */
  public List<String> getNotExportedSearchableFields() {
    return Collections.unmodifiableList(notExportedSearchableFields);
  }


  /**
   * Adds a field to the list of not exported but searchable fields.
   * @param fieldName a field that should be searchable ,but not exported.
   */
  protected void addNotExportableSearchableField(String fieldName) {
    notExportedSearchableFields.add(fieldName);
  }
}