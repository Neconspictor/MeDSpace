package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

import de.unipassau.medspace.wrapper.image_wrapper.ddsm.DDSM_CaseIdentifiable;

/**
 * TODO
 */
public abstract class LesionType extends DDSM_CaseIdentifiable {

  /**
   * TODO
   */
  public static final String LESION_TYPE = "LESION_TYPE";


  /**
   * TODO
   */
  protected String lesionType;


  /**
   * TODO
   * @param lesionType
   */
  public LesionType(String lesionType, String id, String caseName) {
    super(id,caseName);
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