package de.unipassau.medspace.common.multimedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class MultiMediaFile {

  /**
   * The multi-media file
   */
  private File source;

  /**
   * Meta-Data files
   */
  private List<File> metaData;

  /**
   * TODO
   */
  public MultiMediaFile() {
    metaData = new ArrayList<>();
  }

  /**
   * TODO
   * @return
   */
  public File getSource() {
    return source;
  }

  /**
   * TODO
   * @param source
   */
  public void setSource(File source) {
    this.source = source;
  }

  /**
   * TODO
   * @return
   */
  public List<File> getMetaData() {
    return metaData;
  }

  /**
   * TODO
   * @param metaData
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
