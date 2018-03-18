package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.rdf.mapping.IdentifiableFile;

import java.io.File;

/**
 * TODO
 */
public class DDSM_Image extends IdentifiableFile {

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
  public DDSM_Image(File source, OverlayMetaData overlay, String id) {
    super(id, source);
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