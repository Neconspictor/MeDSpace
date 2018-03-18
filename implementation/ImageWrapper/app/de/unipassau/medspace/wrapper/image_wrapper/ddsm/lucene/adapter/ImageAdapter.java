package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.lucene.rdf.LuceneDocFileAdapter;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.ImageMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.OverlayMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.DDSM_Image;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.OverlayMetaData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TODO
 */
public class ImageAdapter extends LuceneDocDdsmCaseAdapter<DDSM_Image> {

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
  private final ImageMapping imageParsing;

  /**
   * TODO
   */
  private final OverlayMapping overlayParsing;

  private static final String IMAGE_FOLDER_STRUCTURE_METADATA = "IMAGE_FOLDER_STRUCTURE_METADATA";

  /**
   * TODO
   * @param imageParsing
   * @param overlayParsing
   */
  public ImageAdapter(ImageMapping imageParsing,
                      OverlayMapping overlayParsing,
                      File root,
                      String ddsmCaseName) {

    this(imageParsing, overlayParsing, ddsmCaseName,
        new LuceneDocFileAdapter<>(imageParsing, root, null));

  }

  protected ImageAdapter(ImageMapping imageParsing,
                      OverlayMapping overlayParsing,
                      String ddsmCaseName,
                      LuceneClassAdapter<DDSM_Image> decorator) {
    super(imageParsing, ddsmCaseName, decorator);
    this.overlayParsing = overlayParsing;

    addPair(SOURCE, imageParsing.getSource());
    addPair(OVERLAY, imageParsing.getOverlay());
    this.imageParsing = imageParsing;

    this.metaDataFields.add(IMAGE_FOLDER_STRUCTURE_METADATA);
  }

  @Override
  protected void addFields(DDSM_Image source, Document doc) throws IOException {

    OverlayMetaData overlay = source.getOverlay();
    if (overlay != null) {
      String id = source.getOverlay().getId();
      doc.add(createField(OVERLAY, id));
    }
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();
    String value = field.stringValue();

    if (isOverlayReference(property)) {
      return overlayParsing.getRdfType() + "#" + value;
    }
    return null;
  }

  /**
   * TODO
   * @param property
   * @return
   */
  private boolean isOverlayReference(PropertyMapping property) {
    if (imageParsing.getOverlay().equals(property)) return true;
    return false;
  }
}