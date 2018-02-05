package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

/**
 * TODO
 */
public class Mass extends LesionType {

  /**
   * TODO
   */
  public static final String MASS = "MASS";

  /**
   * TODO
   */
  private String shape;

  /**
   * TODO
   */
  private String margins;

  /**
   * TODO
   */
  public Mass(String shape, String margins) {
    super(MASS);
    this.shape = shape;
    this.margins = margins;
  }


  /**
   * TODO
   */
  public String getShape() {
    return shape;
  }

  /**
   * TODO
   */
  public String getMargins() {
    return margins;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Mass: {\n");
    builder.append("lesionType: " + lesionType + "\n");
    builder.append("shape: " + shape + "\n");
    builder.append("margins: " + margins + "\n");

    builder.append("}");

    return builder.toString();
  }
}