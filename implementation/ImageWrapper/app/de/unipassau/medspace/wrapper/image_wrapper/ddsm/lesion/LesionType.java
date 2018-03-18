package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion;


import de.unipassau.medspace.common.rdf.mapping.Identifiable;

/**
 * TODO
 */
public abstract class LesionType extends Identifiable {

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