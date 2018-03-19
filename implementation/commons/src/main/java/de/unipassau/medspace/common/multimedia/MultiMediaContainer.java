package de.unipassau.medspace.common.multimedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class MultiMediaContainer {

  /**
   * TODO
   */
  private List<MultiMediaFile> data;

  /**
   * TODO
   */
  private List<File> metaData;

  /**
   * TODO
   */
  public MultiMediaContainer() {
    data = new ArrayList<>();
    metaData = new ArrayList<>();
  }

  /**
   * TODO
   * @return
   */
  public List<MultiMediaFile> getData() {
    return data;
  }

  /**
   * TODO
   * @param data
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

  public List<File> getMetaData() {
    return metaData;
  }

  public void setMetaData(List<File> metaData) {
    this.metaData = metaData;
  }
}
