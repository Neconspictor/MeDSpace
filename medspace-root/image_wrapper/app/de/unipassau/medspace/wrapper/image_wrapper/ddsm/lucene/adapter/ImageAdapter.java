package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneDocFileAdapter;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.register.Service;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.ImageMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.OverlayMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Image;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.OverlayMetaData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;

/**
 * A DDSM adapter for an image file.
 */
public class ImageAdapter extends DDSM_CaseAdapter<Image> {

  private static final String SOURCE = "SOURCE";

  private static final String OVERLAY = "OVERLAY";

  private static final String IMAGE_FOLDER_STRUCTURE_METADATA = "IMAGE_FOLDER_STRUCTURE_METADATA";

  private final ImageMapping imageParsing;

  private final OverlayMapping overlayParsing;


  /**
   * Creates a new ImageAdapter object.
   * @param imageMapping The mapping for an image file.
   * @param overlayMapping The mapping for an overlay file.
   * @param root The DDSM root folder.
   * @param downloadService The base URL of the downloading service
   */
  public ImageAdapter(ImageMapping imageMapping,
                      OverlayMapping overlayMapping,
                      File root,
                      String downloadService) {

    super(imageMapping,
        new LuceneDocFileAdapter<>(imageMapping, root, downloadService, null));
    this.overlayParsing = overlayMapping;

    //addPair(SOURCE, imageParsing.getSource());
    addPair(OVERLAY, imageMapping.getOverlay());
    this.imageParsing = imageMapping;

    this.metaDataFields.add(IMAGE_FOLDER_STRUCTURE_METADATA);

  }

  @Override
  protected void addFields(Image source, Document doc) throws IOException {

    OverlayMetaData overlay = source.getOverlay();
    if (overlay != null) {
      String id = source.getOverlay().getId();
      doc.add(createField(OVERLAY, id));
    }
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();

    if (isOverlayReference(property)) {
      return overlayParsing.getRdfType() + "#" + field.stringValue();
    }
    return null;
  }

  private boolean isOverlayReference(PropertyMapping property) {
    if (imageParsing.getOverlay().equals(property)) return true;
    return false;
  }
}