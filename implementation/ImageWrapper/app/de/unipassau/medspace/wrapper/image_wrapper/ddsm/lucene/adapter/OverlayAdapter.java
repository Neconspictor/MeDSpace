package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.AbnormalityMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.OverlayMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Abnormality;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.OverlayMetaData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * A DDSM adapter for an overlay file.
 */
public class OverlayAdapter extends DDSM_CaseAdapter<OverlayMetaData> {


  private static final String TOTAL_ABNORMALITIES = "TOTAL_ABNORMALITIES";

  private static final String ABNORMALITY = "ABNORMALITY";


  private final OverlayMapping overlayParsing;

  private final AbnormalityMapping abnormalityParsing;

  /**
   * Creates a new OverlayAdapter object.
   *
   * @param overlayMapping The mapping for an overlay file.
   * @param abnormalityMapping The mapping for an abnormality.
   */
  protected OverlayAdapter(OverlayMapping overlayMapping,
                           AbnormalityMapping abnormalityMapping) {
    super(overlayMapping, null);
    this.overlayParsing = overlayMapping;
    this.abnormalityParsing = abnormalityMapping;

    addPair(TOTAL_ABNORMALITIES, overlayMapping.getTotalAbnormalities());
    addPair(ABNORMALITY, overlayMapping.getAbnormality());
  }

  @Override
  protected void addFields(OverlayMetaData source, Document doc) throws IOException {
    //IndexableField[] test = doc.getFields("");

    doc.add(createField(TOTAL_ABNORMALITIES, source.getTotalAbnormalities()));

    for (Abnormality abnormality : source.getAbnormalities()) {
      String value = abnormality.getId();
      doc.add(createField(ABNORMALITY, value));
    }
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();

    if (isAbnormalityReference(property)) {
      return abnormalityParsing.getRdfType() + "#" + field.stringValue();
    }
    return null;
  }

  private boolean isAbnormalityReference(PropertyMapping property) {

    if (overlayParsing.getAbnormality().equals(property)) return true;
    return false;
  }
}