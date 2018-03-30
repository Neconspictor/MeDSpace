package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import java.io.File;

/**
 * Represents an image of a  DDSM case.
 */
public class Image extends DDSM_CaseFile {

  private final File source;

  private final OverlayMetaData overlay;

  /**
   * Creates a new Image object.
   * @param source The source file image.
   * @param overlay The overlay meta data for this image.
   * @param id The ID of this image.
   * @param caseName The name of the case this objects belongs to.
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
   * Provides the overlay meta data.
   * @return the overlay meta data.
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