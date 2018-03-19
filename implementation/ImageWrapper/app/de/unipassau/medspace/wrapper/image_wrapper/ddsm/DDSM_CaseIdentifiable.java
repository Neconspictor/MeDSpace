package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.rdf.mapping.Identifiable;

/**
 * TODO
 */
public class DDSM_CaseIdentifiable implements Identifiable {

  protected final String id;

  protected final String caseName;

  /**
   * TODO
   * @param id
   * @param caseName
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
   * TODO
   * @return
   */
  public String getCaseName() {
    return caseName;
  }
}