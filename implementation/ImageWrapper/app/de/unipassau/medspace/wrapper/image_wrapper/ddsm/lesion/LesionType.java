package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;


import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Identifiable;

/**
 * TODO
 */
public abstract class LesionType extends Identifiable {

  /**
   * TODO
   */
  protected String lesionType;


  /**
   * TODO
   * @param lesionType
   */
  public LesionType(String lesionType, String id) {
    super(id);
    this.lesionType = lesionType;
  }

  /**
   * TODO
   * @return
   */
  public String getLesionType() {
    return lesionType;
  }
}