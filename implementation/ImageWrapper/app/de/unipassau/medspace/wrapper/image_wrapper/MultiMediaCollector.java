package de.unipassau.medspace.wrapper.image_wrapper;

import java.io.File;
import java.util.List;

/**
 * TODO
 */
public interface MultiMediaCollector {

  /**
   * TODO
   * @param root
   * @return
   */
  List<MultiMediaContainer> collect(File root);
}