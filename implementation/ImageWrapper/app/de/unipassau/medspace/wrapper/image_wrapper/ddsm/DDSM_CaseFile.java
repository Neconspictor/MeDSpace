package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.rdf.mapping.IdentifiableFile;

import java.io.File;

/**
 * TODO
 */
public class DDSM_CaseFile extends DDSM_CaseIdentifiable implements IdentifiableFile {

  protected final File source;

  /**
   * TODO
   *
   * @param id
   * @param caseName
   * @param source
   */
  public DDSM_CaseFile(String id, String caseName, File source) {
    super(id, caseName);
    this.source = source;
  }

  @Override
  public File getSource() {
    return source;
  }
}