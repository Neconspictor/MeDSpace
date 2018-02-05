package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

/**
 * TODO
 */
public abstract class LesionType {

  /**
   * TODO
   */
  protected String lesionType;

  /**
   * TODO
   */
  protected String id;


  /**
   * TODO
   * @param lesionType
   */
  public LesionType(String lesionType) {
    this.lesionType = lesionType;
  }

  /**
   * TODO
   * @return
   */
  public String getLesionType() {
    return lesionType;
  }

  /**
   * TODO
   */
  public String getId() {
    return id;
  }
}