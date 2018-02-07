package de.unipassau.medspace.wrapper.image_wrapper.ddsm;


import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;

import java.util.List;

/**
 * TODO
 */
public class Abnormality extends Identifiable {

  /**
   * TODO
   */
  private int abnormalityNumber;

  /**
   * TODO
   */
  private List<LesionType> lesionTypes;

  /**
   * TODO
   */
  private int assessment;

  /**
   * TODO
   */
  private int subtlety;

  /**
   * TODO
   */
  private String pathology;

  /**
   * TODO
   */
  private int totalOutlines;

  /**
   * TODO
   * @param abnormalityNumber
   * @param lesionTypes
   * @param assessment
   * @param subtlety
   * @param pathology
   * @param totalOutlines
   */
  public Abnormality(int abnormalityNumber,
                     List<LesionType> lesionTypes,
                     int assessment,
                     int subtlety,
                     String pathology,
                     int totalOutlines,
                     String id) {
    super(id);

    this.abnormalityNumber = abnormalityNumber;
    this.lesionTypes = lesionTypes;
    this.assessment = assessment;
    this.pathology = pathology;
    this.totalOutlines = totalOutlines;
  }

  /**
   * TODO
   */
  public int getAbnormalityNumber() {
    return abnormalityNumber;
  }

  /**
   * TODO
   */
  public List<LesionType> getLesionTypes() {
    return lesionTypes;
  }

  /**
   * TODO
   */
  public int getAssessment() {
    return assessment;
  }

  /**
   * TODO
   */
  public String getPathology() {
    return pathology;
  }

  /**
   * TODO
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
   * TODO
   */
  public int getSubtlety() {
    return subtlety;
  }
}