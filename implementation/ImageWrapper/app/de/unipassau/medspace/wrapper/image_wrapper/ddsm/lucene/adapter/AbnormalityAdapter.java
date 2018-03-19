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
 * TODO
 */
public class AbnormalityAdapter extends DDSM_CaseAdapter<Abnormality> {


  /**
   * TODO
   */
  public static final String ABNORMALITY = "ABNORMALITY";

  /**
   * TODO
   */
  public static final String CALCIFICATION = "CALCIFICATION";

  /**
   * TODO
   */
  public static final String MASS = "MASS";

  /**
   * TODO
   */
  public static final String ASSESSMENT = "ASSESSMENT";

  /**
   * TODO
   */
  public static final String SUBTLETY = "SUBTLETY";

  /**
   * TODO
   */
  public static final String PATHOLOGY = "PATHOLOGY";

  /**
   * TODO
   */
  public static final String TOTAL_OUTLINES = "TOTAL_OUTLINES";


  /**
   * TODO
   */
  private final AbnormalityMapping abnormalityParsing;

  /**
   * TODO
   */
  private final CalcificationMapping calcificationParsing;

  /**
   * TODO
   */
  private final MassMapping massParsing;

  /**
   * TODO
   *
   * @param abnormalityParsing
   * @param calcificationParsing
   * @param massParsing
   */
  protected AbnormalityAdapter(AbnormalityMapping abnormalityParsing,
                               CalcificationMapping calcificationParsing,
                               MassMapping massParsing) {
    super(abnormalityParsing, null);
    this.abnormalityParsing = abnormalityParsing;
    this.calcificationParsing = calcificationParsing;
    this.massParsing = massParsing;

    addPair(ABNORMALITY, abnormalityParsing.getAbnormality());
    addPair(CALCIFICATION, abnormalityParsing.getCalcification());
    addPair(MASS, abnormalityParsing.getMass());
    addPair(ASSESSMENT, abnormalityParsing.getAssessment());
    addPair(SUBTLETY, abnormalityParsing.getSubtlety());
    addPair(PATHOLOGY, abnormalityParsing.getPathology());
    addPair(TOTAL_OUTLINES, abnormalityParsing.getTotalOutlines());
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