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
 * TODO
 */
public class OverlayAdapter extends LuceneDocDdsmCaseAdapter<OverlayMetaData> {

  /**
   * TODO
   */
  public static final String TOTAL_ABNORMALITIES = "TOTAL_ABNORMALITIES";

  /**
   * TODO
   */
  public static final String ABNORMALITY = "ABNORMALITY";

  /**
   * TODO
   */
  private final OverlayMapping overlayParsing;

  /**
   * TODO
   */
  private final AbnormalityMapping abnormalityParsing;

  /**
   * TODO
   *
   * @param overlayParsing
   * @param abnormalityParsing
   */
  protected OverlayAdapter(OverlayMapping overlayParsing,
                           AbnormalityMapping abnormalityParsing,
                           String ddsmCaseName) {
    super(overlayParsing,ddsmCaseName);
    this.overlayParsing = overlayParsing;
    this.abnormalityParsing = abnormalityParsing;

    addPair(TOTAL_ABNORMALITIES, overlayParsing.getTotalAbnormalities());
    addPair(ABNORMALITY, overlayParsing.getAbnormality());
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
  public String createValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();
    String value = field.stringValue();

    if (isAbnormalityReference(property)) {
      value = abnormalityParsing.getRdfType() + "#" + value;
    }

    return value;
  }

  /**
   * TODO
   * @param property
   * @return
   */
  private boolean isAbnormalityReference(PropertyMapping property) {

    if (overlayParsing.getAbnormality().equals(property)) return true;
    return false;
  }
}