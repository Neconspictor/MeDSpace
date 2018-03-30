package de.unipassau.medspace.common.multimedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A multimedia container is used to group multimedia files that belong together.
 */
public class MultiMediaContainer {

  private List<MultiMediaFile> data;

  private List<File> metaData;

  /**
   * Creates a new MultiMediaContainer.
   */
  public MultiMediaContainer() {
    data = new ArrayList<>();
    metaData = new ArrayList<>();
  }

  /**
   * Provides the list of multimedia files.
   * @return the list of multimedia files.
   */
  public List<MultiMediaFile> getData() {
    return data;
  }

  /**
   * Sets the list of multimedia files.
   * @param data the list of multimedia files.
   */
  public void setData(List<MultiMediaFile> data) {
    this.data = data;
  }


  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MultiMediaContainer {\n");

    for (File file : metaData) {
      builder.append("Meta data file: " + file.getAbsolutePath() + "\n");
    }

    for (MultiMediaFile multiMediaFile : data) {
      builder.append(multiMediaFile + "\n");
    }
    builder.append("}");

    return builder.toString();
  }

  /**
   * Provides the list of meta data files.
   * @return the list of meta data files.
   */
  public List<File> getMetaData() {
    return metaData;
  }

  /**
   * Sets the lists of multimedia files.
   * @param metaData the lists of multimedia files.
   */
  public void setMetaData(List<File> metaData) {
    this.metaData = metaData;
  }
}