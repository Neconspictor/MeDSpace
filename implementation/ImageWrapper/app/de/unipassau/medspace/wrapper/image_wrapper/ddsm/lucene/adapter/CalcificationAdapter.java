package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.CalcificationParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public class CalcificationAdapter extends LuceneDocAdapter<Calcification> {

  /**
   * TODO
   */
  public static final String TYPE = "TYPE";

  /**
   * TODO
   */
  public static final String DISTRIBUTION = "DISTRIBUTION";


  /**
   * TODO
   *
   * @param calcificationParsing
   */
  protected CalcificationAdapter(CalcificationParsing calcificationParsing) {
    super(calcificationParsing);

    addPair(TYPE, calcificationParsing.getType());
    addPair(DISTRIBUTION, calcificationParsing.getDistribution());
  }

  @Override
  protected void addFields(Calcification source, Document doc) throws IOException {
    doc.add(createField(TYPE, source.getType()));
    doc.add(createField(DISTRIBUTION, source.getDistribution()));
  }

  @Override
  public String createValue(Pair<String, Property> pair, IndexableField field) {
    return field.stringValue();
  }
}