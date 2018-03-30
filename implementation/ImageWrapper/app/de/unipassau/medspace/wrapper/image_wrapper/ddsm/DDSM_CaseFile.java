package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.rdf.mapping.IdentifiableFile;

import java.io.File;

/**
 * Represents a DDSM case file.
 */
public class DDSM_CaseFile extends DDSM_CaseIdentifiable implements IdentifiableFile {

  /**
   * The source file.
   */
  protected final File source;

  /**
   * Creates a new DDSM_CaseFile object.
   * @param id the ID of this case file.
   * @param caseName The name of the case this object belongs to.
   * @param source The source file.
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