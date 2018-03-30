package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.rdf.mapping.Identifiable;

/**
 * Represents an identifiable of a DDSM case.
 */
public class DDSM_CaseIdentifiable implements Identifiable {

  /**
   * The id
   */
  protected final String id;

  /**
   * The name of the case this identifiable belongs to.
   */
  protected final String caseName;

  /**
   * Creates a new DDSM_CaseIdentifiable object.
   * @param id the ID of this case file.
   * @param caseName The name of the case this lesion belongs to.
   */
  public DDSM_CaseIdentifiable(String id, String caseName) {
    this.id = id;
    this.caseName = caseName;
  }

  @Override
  public String getId() {
    return id;
  }

  /**
   * Provides the name of the case this identifiable belongs to.
   * @return the name of the case this identifiable belongs to.
   */
  public String getCaseName() {
    return caseName;
  }
}