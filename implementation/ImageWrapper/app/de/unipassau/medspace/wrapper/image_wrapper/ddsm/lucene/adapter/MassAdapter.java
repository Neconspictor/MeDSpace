package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.MassMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * A DDSM adapter for a mass.
 */
public class MassAdapter extends DDSM_CaseAdapter<Mass> {

  private static final String SHAPE = "SHAPE";

  private static final String MARGINS = "MARGINS";


  /**
   * Creates a new MassAdapter object.
   * @param massParsing The mapping for a mass.
   */
  public MassAdapter(MassMapping massParsing) {
    super(massParsing, null);

    addPair(SHAPE, massParsing.getShape());
    addPair(MARGINS, massParsing.getMargins());
  }

  @Override
  protected void addFields(Mass source, Document doc) throws IOException {
    doc.add(createField(SHAPE, source.getShape()));
    doc.add(createField(MARGINS, source.getMargins()));
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {

    // use default value
    return null;
  }
}