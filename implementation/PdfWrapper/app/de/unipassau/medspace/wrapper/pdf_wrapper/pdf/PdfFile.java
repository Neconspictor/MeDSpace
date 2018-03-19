package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;

import de.unipassau.medspace.common.rdf.mapping.IdentifiableFile;

import java.io.File;

/**
 * TODO
 */
public class PdfFile implements IdentifiableFile {


  private final String id;

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
    this.id = id;
    this.source = source;
  }

  @Override
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

  @Override
  public String getId() {
    return id;
  }
}