package de.unipassau.medspace.common.multimedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a multimedia file. A multimedia file contains of a multimedia source file and (optional)
 * additionally meta data files.
 */
public class MultiMediaFile {

  /**
   * The multimedia file
   */
  private File source;

  /**
   * Meta-Data files
   */
  private List<File> metaData;

  /**
   * Creates a new multimedia file.
   */
  public MultiMediaFile() {
    metaData = new ArrayList<>();
  }

  /**
   * Provides the source file.
   * @return the source file.
   */
  public File getSource() {
    return source;
  }

  /**
   * Sets the source file.
   * @param source the source file.
   */
  public void setSource(File source) {
    this.source = source;
  }

  /**
   * Provides the the meta data file list.
   * @return the the meta data file list.
   */
  public List<File> getMetaData() {
    return metaData;
  }

  /**
   * Sets the the meta data file list.
   * @param metaData the the meta data file list.
   */
  public void setMetaData(List<File> metaData) {
    this.metaData = metaData;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MultiMediaFile {\n");
    builder.append("\t");
    builder.append("Source file: " + source.getAbsolutePath() + "\n");
    for (File metaDataFile : metaData) {
      builder.append("\t");
      builder.append("Meta data file: " + metaDataFile.getAbsolutePath() + "\n");
    }
    builder.append("}");

    return builder.toString();
  }
}