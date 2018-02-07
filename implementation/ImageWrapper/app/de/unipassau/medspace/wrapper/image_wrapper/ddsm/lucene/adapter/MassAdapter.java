package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.MassParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public class MassAdapter extends LuceneDocAdapter<Mass> {

  /**
   * TODO
   */
  public static final String SHAPE = "SHAPE";

  /**
   * TODO
   */
  public static final String MARGINS = "MARGINS";


  /**
   * TODO
   * @param massParsing
   */
  protected MassAdapter(MassParsing massParsing) {
    super(massParsing);

    addPair(SHAPE, massParsing.getShape());
    addPair(MARGINS, massParsing.getMargins());
  }

  @Override
  protected void addFields(Mass source, Document doc) throws IOException {
    doc.add(createField(SHAPE, source.getShape()));
    doc.add(createField(MARGINS, source.getMargins()));
  }

  @Override
  public String createValue(Pair<String, Property> pair, IndexableField field) {
    return field.stringValue();
  }
}