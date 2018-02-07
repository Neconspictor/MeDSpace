package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.AbnormalityParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.OverlayParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Abnormality;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.OverlayMetaData;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public class OverlayAdapter extends LuceneDocAdapter<OverlayMetaData> {

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
  private final OverlayParsing overlayParsing;

  /**
   * TODO
   */
  private final AbnormalityParsing abnormalityParsing;

  /**
   * TODO
   *
   * @param overlayParsing
   * @param abnormalityParsing
   */
  protected OverlayAdapter(OverlayParsing overlayParsing, AbnormalityParsing abnormalityParsing) {
    super(overlayParsing);
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
  public String createValue(Pair<String, Property> pair, IndexableField field) {
    Property property = pair.getValue1();
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
  private boolean isAbnormalityReference(Property property) {

    if (overlayParsing.getAbnormality().equals(property)) return true;
    return false;
  }
}