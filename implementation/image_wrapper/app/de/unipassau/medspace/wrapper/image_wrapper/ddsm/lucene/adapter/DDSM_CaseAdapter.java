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
 * An abstract adapter for a DDSM case.
 */
public abstract class DDSM_CaseAdapter<ClassType extends DDSM_CaseIdentifiable>
    extends LuceneClassAdapter<ClassType> {

  private static final String DDSM_CASE_META_DATA_TAG = "DDSM_CASE_META_DATA_TAG";

  /**
   * Creates a new DDSM_CaseAdapter object.
   * @param classMapping The RDF class mapping.
   * @param decorator Another LuceneClassAdapter that should be used as a decorator.
   */
  public DDSM_CaseAdapter(ClassMapping classMapping,
                          LuceneClassAdapter<? super ClassType> decorator) {
    super(classMapping, decorator);

    metaDataFields.add(DDSM_CASE_META_DATA_TAG);
  }


  /**
   * Creates a new DDSM_CaseAdapter object.
   * @param decorator Another LuceneClassAdapter that should be used as a decorator.
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