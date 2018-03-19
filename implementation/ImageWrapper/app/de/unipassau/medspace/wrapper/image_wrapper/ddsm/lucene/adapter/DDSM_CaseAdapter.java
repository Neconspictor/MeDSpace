package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.rdf.mapping.ClassMapping;
import de.unipassau.medspace.common.rdf.mapping.Identifiable;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.DDSM_CaseIdentifiable;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public abstract class DDSM_CaseAdapter<ClassType extends DDSM_CaseIdentifiable>
    extends LuceneClassAdapter<ClassType> {


  /**
   * TODO
   */
  public static final String DDSM_CASE_META_DATA_TAG = "DDSM_CASE_META_DATA_TAG";

  /**
   * TODO
   */
  public DDSM_CaseAdapter(ClassMapping classMapping,
                          LuceneClassAdapter<? super ClassType> decorator) {
    super(classMapping, decorator);

    metaDataFields.add(DDSM_CASE_META_DATA_TAG);
  }


  /**
   * TODO
   */
  public DDSM_CaseAdapter(LuceneClassAdapter<ClassType> decorator) {
    super(decorator);

    metaDataFields.add(DDSM_CASE_META_DATA_TAG);
  }


  @Override
  public Document convert(ClassType source) throws IOException {
    Document document = super.convert(source);
    document.add(createField(DDSM_CASE_META_DATA_TAG, source.getCaseName()));
    return document;
  }
}