package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import java.io.File;

/**
 * TODO
 */
public class Image extends DDSM_CaseFile {

  /**
   * TODO
   */
  private final File source;

  /**
   * TODO
   */
  private final OverlayMetaData overlay;

  /**
   * TODO
   * @param source
   * @param overlay
   * @param id
   */
  public Image(File source,
               OverlayMetaData overlay,
               String id,
               String caseName) {
    super(id, caseName, source);
    this.source = source;
    this.overlay = overlay;
  }

  /**
   * TODO
   */
  public File getSource() {
    return source;
  }

  /**
   * TODO
   */
  public OverlayMetaData getOverlay() {
    return overlay;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("DDSM_IMAGE: {\n");
    builder.append("source: " + source.getAbsolutePath() + "\n");
    if (overlay != null) builder.append("overlay: " + overlay + "\n");
    builder.append("id: " + id + "\n");
    builder.append("}");

    return builder.toString();
  }
}