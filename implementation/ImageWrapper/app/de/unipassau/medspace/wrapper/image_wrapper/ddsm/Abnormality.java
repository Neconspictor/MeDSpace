package de.unipassau.medspace.wrapper.image_wrapper.ddsm;


import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;

import java.util.List;

/**
 * Represents an abnormality.
 */
public class Abnormality extends DDSM_CaseIdentifiable {

  /**
   * The ABNORMALITY token in an overlay file.
   */
  public static final String ABNORMALITY = "ABNORMALITY";

  /**
   * The ASSESSMENT token in an overlay file.
   */
  public static final String ASSESSMENT = "ASSESSMENT";

  /**
   * The SUBTLETY token in an overlay file.
   */
  public static final String SUBTLETY = "SUBTLETY";

  /**
   * The PATHOLOGY token in an overlay file.
   */
  public static final String PATHOLOGY = "PATHOLOGY";

  /**
   * The TOTAL_OUTLINES token in an overlay file.
   */
  public static final String TOTAL_OUTLINES = "TOTAL_OUTLINES";



  private int abnormalityNumber;

  private List<LesionType> lesionTypes;

  private int assessment;

  private int subtlety;

  private String pathology;

  private int totalOutlines;

  /**
   * Creates a new Abnormality object.
   * @param abnormalityNumber he abnormality number.
   * @param lesionTypes the lesion types.
   * @param assessment the assessment.
   * @param subtlety the subtlety
   * @param pathology the pathology
   * @param totalOutlines the total outline number
   * @param id the ID of this abnormality
   * @param caseName The name of the case this objects belongs to.
   */
  public Abnormality(int abnormalityNumber,
                     List<LesionType> lesionTypes,
                     int assessment,
                     int subtlety,
                     String pathology,
                     int totalOutlines,
                     String id,
                     String caseName) {
    super(id, caseName);

    this.abnormalityNumber = abnormalityNumber;
    this.lesionTypes = lesionTypes;
    this.assessment = assessment;
    this.pathology = pathology;
    this.totalOutlines = totalOutlines;
  }

  /**
   * Provides the abnormality number.
   * @return the abnormality number.
   */
  public int getAbnormalityNumber() {
    return abnormalityNumber;
  }

  /**
   * Provides the lesion types.
   * @return the lesion types.
   */
  public List<LesionType> getLesionTypes() {
    return lesionTypes;
  }

  /**
   * Provides the assessment
   * @return the assessment
   */
  public int getAssessment() {
    return assessment;
  }

  /**
   * Provides the pathology
   * @return the pathology
   */
  public String getPathology() {
    return pathology;
  }

  /**
   * Provides the total outlines.
   * @return the total outlines.
   */
  public int getTotalOutlines() {
    return totalOutlines;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Abnormality: {\n");

    builder.append("abnormalityNumber: " + abnormalityNumber + "\n");
    for (LesionType type : lesionTypes) {
      builder.append("lesionType: " + type + "\n");
    }
    builder.append("assessment: " + assessment + "\n");
    builder.append("subtlety: " + subtlety + "\n");
    builder.append("pathology: " + pathology + "\n");
    builder.append("totalOutlines: " + totalOutlines + "\n");

    builder.append("}");

    return builder.toString();
  }

  /**
   * Provides the subtlety.
   * @return the subtlety.
   */
  public int getSubtlety() {
    return subtlety;
  }
}