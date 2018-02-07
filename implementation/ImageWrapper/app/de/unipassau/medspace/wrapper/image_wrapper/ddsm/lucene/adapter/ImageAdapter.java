package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.ImageParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.OverlayParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.DDSM_Image;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.OverlayMetaData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public class ImageAdapter extends LuceneDocAdapter<DDSM_Image> {

  /**
   * TODO
   */
  public static final String SOURCE = "SOURCE";

  /**
   * TODO
   */
  public static final String OVERLAY = "OVERLAY";

  /**
   * TODO
   */
  private final ImageParsing imageParsing;

  /**
   * TODO
   */
  private final OverlayParsing overlayParsing;

  /**
   * TODO
   * @param imageParsing
   * @param overlayParsing
   */
  public ImageAdapter(ImageParsing imageParsing, OverlayParsing overlayParsing) {

    super(imageParsing);
    this.overlayParsing = overlayParsing;

    addPair(SOURCE, imageParsing.getSource());
    addPair(OVERLAY, imageParsing.getOverlay());
    this.imageParsing = imageParsing;
  }

  @Override
  protected void addFields(DDSM_Image source, Document doc) throws IOException {
    String value = source.getSource().toURI().toString();
    doc.add(createField(SOURCE, value));

    OverlayMetaData overlay = source.getOverlay();
    if (overlay != null) {
      String id = source.getOverlay().getId();
      doc.add(createField(OVERLAY, id));
    }
  }

  @Override
  public String createValue(Pair<String, Property> pair, IndexableField field) {
    Property property = pair.getValue1();
    String value = field.stringValue();

    if (isOverlayReference(property)) {
      value = overlayParsing.getRdfType() + "#" + value;
    }

    return value;
  }

  /**
   * TODO
   * @param property
   * @return
   */
  private boolean isOverlayReference(Property property) {
    if (imageParsing.getOverlay().equals(property)) return true;
    return false;
  }
}