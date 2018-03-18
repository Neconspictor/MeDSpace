package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

/**
 * TODO
 */
public class Calcification extends LesionType {

  /**
   * TODO
   */
  public static final String CALCIFICATION = "CALCIFICATION";


  /**
   * TODO
   */
  public static final String TYPE = "TYPE";

  /**
   * TODO
   */
  public static final String DISTRIBUTION = "DISTRIBUTION";

  /**
   * TODO
   */
  private String type;

  /**
   * TODO
   */
  private String distribution;


  /**
   * TODO
   */
  public Calcification(String type, String distribution, String id) {
    super(CALCIFICATION, id);

    this.type = type;
    this.distribution = distribution;
  }

  /**
   * TODO
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   * TODO
   * @return
   */
  public String getDistribution() {
    return distribution;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Calcification: {\n");
    builder.append("lesionType: " + lesionType + "\n");
    builder.append("type: " + type + "\n");
    builder.append("distribution: " + distribution + "\n");

    builder.append("}");

    return builder.toString();
  }
}