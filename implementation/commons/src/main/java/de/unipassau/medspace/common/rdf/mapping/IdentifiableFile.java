package de.unipassau.medspace.common.rdf.mapping;

import java.io.File;

/**
 * An identifiable which also is a file.
 */
public interface IdentifiableFile extends Identifiable {

  /**
   * Provides the source file.
   * @return The source file.
   */
  File getSource();
}