package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;

import java.io.File;

/**
 * TODO
 */
public class PdfFile extends Identifiable {

  /**
   * TODO
   */
  private final File source;

  /**
   * TODO
   * @param source
   * @param id
   */
  public PdfFile(File source, String id) {
    super(id);
    this.source = source;
  }

  /**
   * TODO
   */
  public File getSource() {
    return source;
  }


  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("PdfFile: {\n");
    builder.append("source: " + source.getAbsolutePath() + "\n");
    builder.append("id: " + id + "\n");
    builder.append("}");

    return builder.toString();
  }
}