package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.AbnormalityParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.CalcificationParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.MassParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
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
public class AbnormalityAdapter extends LuceneDocAdapter<Abnormality> {


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
  private final AbnormalityParsing abnormalityParsing;

  /**
   * TODO
   */
  private final CalcificationParsing calcificationParsing;

  /**
   * TODO
   */
  private final MassParsing massParsing;

  /**
   * TODO
   *
   * @param abnormalityParsing
   * @param calcificationParsing
   * @param massParsing
   */
  protected AbnormalityAdapter(AbnormalityParsing abnormalityParsing,
                               CalcificationParsing calcificationParsing,
                               MassParsing massParsing) {
    super(abnormalityParsing);
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
  public String createValue(Pair<String, Property> pair, IndexableField field) {
    Property property = pair.getValue1();
    String value = field.stringValue();

    if (isCalcificationReference(property)) {
      value = calcificationParsing.getRdfType() + "#" + value;
    } else if (isMassReference(property)) {
      value = massParsing.getRdfType() + "#" + value;
    }

    return value;
  }

  private boolean isMassReference(Property property) {
    return property.equals(abnormalityParsing.getMass());
  }

  private boolean isCalcificationReference(Property property) {
    return property.equals(abnormalityParsing.getCalcification());
  }
}