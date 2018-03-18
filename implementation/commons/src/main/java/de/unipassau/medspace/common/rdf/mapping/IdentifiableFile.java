package de.unipassau.medspace.common.rdf.mapping;

import java.io.File;

/**
 * TODO
 */
public class IdentifiableFile extends Identifiable {

  protected final File source;

  /**
   * TODO
   *
   * @param id
   * @param source
   */
  public IdentifiableFile(String id, File source) {
    super(id);
    this.source = source;
  }

  /**
   * TODO
   * @return
   */
  public File getSource() {
    return source;
  }
}
