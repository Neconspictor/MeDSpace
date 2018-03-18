package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.rdf.mapping.ClassMapping;
import de.unipassau.medspace.common.rdf.mapping.Identifiable;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public abstract class LuceneDocDdsmCaseAdapter<ClassType extends Identifiable>
    extends LuceneClassAdapter<ClassType> {


  /**
   * TODO
   */
  public static final String DDSM_CASE_META_DATA_TAG = "DDSM_CASE_META_DATA_TAG";



  protected final String ddsmCaseName;

  /**
   * TODO
   */
  public LuceneDocDdsmCaseAdapter(ClassMapping classMapping,
                                     String ddsmCaseName,
                                     LuceneClassAdapter<ClassType> decorator) {
    super(classMapping, decorator);

    metaDataFields.add(DDSM_CASE_META_DATA_TAG);
    this.ddsmCaseName = ddsmCaseName;
  }


  /**
   * TODO
   */
  public LuceneDocDdsmCaseAdapter(String ddsmCaseName, LuceneClassAdapter<ClassType> decorator) {
    super(decorator);

    metaDataFields.add(DDSM_CASE_META_DATA_TAG);
    this.ddsmCaseName = ddsmCaseName;
  }


  @Override
  public Document convert(ClassType source) throws IOException {
    Document document = super.convert(source);
    document.add(createField(DDSM_CASE_META_DATA_TAG, ddsmCaseName));
    document.add(createField(DDSM_CASE_META_DATA_TAG, ddsmCaseName));
    return document;
  }
}