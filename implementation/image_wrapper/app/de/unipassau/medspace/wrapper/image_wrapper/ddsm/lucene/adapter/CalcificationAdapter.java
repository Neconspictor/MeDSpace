package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.CalcificationMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * A DDSM adapter for a calcification.
 */
public class CalcificationAdapter extends DDSM_CaseAdapter<Calcification> {

  private static final String TYPE = "TYPE";

  private static final String DISTRIBUTION = "DISTRIBUTION";


  /**
   * Creates a new AbnormalityAdapter object.
   *
   * @param calcificationMapping The mapping for calcification
   */
  protected CalcificationAdapter(CalcificationMapping calcificationMapping) {
    super(calcificationMapping, null);

    addPair(TYPE, calcificationMapping.getType());
    addPair(DISTRIBUTION, calcificationMapping.getDistribution());
  }

  @Override
  protected void addFields(Calcification source, Document doc) throws IOException {
    doc.add(createField(TYPE, source.getType()));
    doc.add(createField(DISTRIBUTION, source.getDistribution()));
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    return null;
  }
}