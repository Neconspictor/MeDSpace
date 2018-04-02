package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

/**
 * Represents a mass.
 */
public class Mass extends LesionType {

  /**
   * The MASS token in an overlay file.
   */
  public static final String MASS = "MASS";

  /**
   * The SHAPE token in an overlay file.
   */
  public static final String SHAPE = "SHAPE";


  /**
   * The MARGINS token in an overlay file.
   */
  public static final String MARGINS = "MARGINS";


  private String shape;

  private String margins;

  /**
   * Creates a new Mass object.
   *
   * @param shape The shape of this mass.
   * @param margins The margins of this mass.
   * @param id The id of this mass.
   * @param caseName The name of the case this object belongs to.
   */
  public Mass(String shape, String margins, String id, String caseName) {
    super(MASS, id, caseName);
    this.shape = shape;
    this.margins = margins;
  }


  /**
   * Provides the shape.
   * @return the shape.
   */
  public String getShape() {
    return shape;
  }

  /**
   * Provides the margins.
   * @return the margins.
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