package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

/**
 * Represents a calcification.
 */
public class Calcification extends LesionType {

  /**
   * The token of a calcification type in an overlay file.
   */
  public static final String CALCIFICATION = "CALCIFICATION";


  /**
   *  The type token in an overlay file.
   */
  public static final String TYPE = "TYPE";

  /**
   *  The distribution token in an overlay file.
   */
  public static final String DISTRIBUTION = "DISTRIBUTION";


  private String type;


  private String distribution;



  /**
   * Creates a new Calcification object.
   * @param type The type value
   * @param distribution The distribution value
   * @param id The calcification id
   * @param caseName The name of the case this calcification belongs to.
   */
  public Calcification(String type, String distribution, String id, String caseName) {
    super(CALCIFICATION, id, caseName);

    this.type = type;
    this.distribution = distribution;
  }

  /**
   * Provides the type value.
   * @return the type value.
   */
  public String getType() {
    return type;
  }

  /**
   * Provides the distribution value.
   * @return the distribution value.
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