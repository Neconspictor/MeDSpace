package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.AbnormalityMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.CalcificationMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.MassMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Abnormality;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * A DDSM adapter for an abnormality.
 */
public class AbnormalityAdapter extends DDSM_CaseAdapter<Abnormality> {

  private static final String ABNORMALITY = "ABNORMALITY";

  private static final String CALCIFICATION = "CALCIFICATION";

  private static final String MASS = "MASS";

  private static final String ASSESSMENT = "ASSESSMENT";

  private static final String SUBTLETY = "SUBTLETY";

  private static final String PATHOLOGY = "PATHOLOGY";

  private static final String TOTAL_OUTLINES = "TOTAL_OUTLINES";



  private final AbnormalityMapping abnormalityParsing;

  private final CalcificationMapping calcificationParsing;

  private final MassMapping massParsing;

  /**
   * Creates a new AbnormalityAdapter object.
   *
   * @param abnormalityMapping The mapping for an abnormality
   * @param calcificationMapping The mapping for calcification
   * @param massMapping The mapping for a mass
   */
  protected AbnormalityAdapter(AbnormalityMapping abnormalityMapping,
                               CalcificationMapping calcificationMapping,
                               MassMapping massMapping) {
    super(abnormalityMapping, null);
    this.abnormalityParsing = abnormalityMapping;
    this.calcificationParsing = calcificationMapping;
    this.massParsing = massMapping;

    addPair(ABNORMALITY, abnormalityMapping.getAbnormality());
    addPair(CALCIFICATION, abnormalityMapping.getCalcification());
    addPair(MASS, abnormalityMapping.getMass());
    addPair(ASSESSMENT, abnormalityMapping.getAssessment());
    addPair(SUBTLETY, abnormalityMapping.getSubtlety());
    addPair(PATHOLOGY, abnormalityMapping.getPathology());
    addPair(TOTAL_OUTLINES, abnormalityMapping.getTotalOutlines());
  }

  @Override
  protected void addFields(Abnormality source, Document doc) throws IOException {
    doc.add(createField(ABNORMALITY, source.getAbnormalityNumber()));
    for (LesionType lesionType : source.getLesionTypes()) {
      if (lesionType instanceof Calcification) {
        doc.add(createField(CALCIFICATION, lesionType.getId()));
      } else {
        doc.add(createField(MASS, lesionType.getId()));
      }
    }
    doc.add(createField(ASSESSMENT, source.getAssessment()));
    doc.add(createField(SUBTLETY, source.getSubtlety()));
    doc.add(createField(PATHOLOGY, source.getPathology()));
    doc.add(createField(TOTAL_OUTLINES, source.getTotalOutlines()));
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();
    String value = null;

    if (isCalcificationReference(property)) {
      value = calcificationParsing.getRdfType() + "#" + field.stringValue();
    } else if (isMassReference(property)) {
      value = massParsing.getRdfType() + "#" + field.stringValue();
    }
    return value;
  }

  private boolean isMassReference(PropertyMapping property) {
    return property.equals(abnormalityParsing.getMass());
  }

  private boolean isCalcificationReference(PropertyMapping property) {
    return property.equals(abnormalityParsing.getCalcification());
  }
}