package de.unipassau.medspace.common.multimedia;

import java.io.File;
import java.util.List;

/**
 * A multimedia collector collects multimedia files from a root folder.
 */
public interface MultiMediaCollector {

  /**
   * Collects multimedia files from a root file. The multimedia files are grouped into containers.
   * @param root The root file.
   * @return A list of multimedia containers.
   */
  List<MultiMediaContainer> collect(File root);
}
