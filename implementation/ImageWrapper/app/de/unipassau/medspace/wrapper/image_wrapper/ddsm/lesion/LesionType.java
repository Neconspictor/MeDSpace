package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;

import de.unipassau.medspace.wrapper.image_wrapper.ddsm.DDSM_CaseIdentifiable;

/**
 * Represents a lesion type.
 */
public abstract class LesionType extends DDSM_CaseIdentifiable {

  /**
   * The LESION_TYPE token in an overlay file.
   */
  public static final String LESION_TYPE = "LESION_TYPE";


  /**
   * The lesion type value
   */
  protected String lesionType;


  /**
   * Creates a new LesionType object.
   *
   * @param lesionType The lesion type.
   * @param id The id of this lesion.
   * @param caseName The name of the case this lesion belongs to.
   */
  public LesionType(String lesionType, String id, String caseName) {
    super(id,caseName);
    this.lesionType = lesionType;
  }

  /**
   * Provides the lesion type value.
   * @return the lesion type value.
   */
  public String getLesionType() {
    return lesionType;
  }
}