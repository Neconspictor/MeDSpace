package de.unipassau.medspace.d2r.bridge;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFObject;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rUtil;

/**
 * D2r Bridge for properties with a data type (Literals).
 */
public class DatatypePropertyBridge extends Bridge {

  public DatatypePropertyBridge(RDFFactory primitiveValueFactory) {
    super(primitiveValueFactory);
  }

  /**
   * Creates a property value from a given SQLResultTuple and normalizes it with the normalizer.
   * The resulting property value will be an rdf literal.
   * @param tuple The sql result tuple to get the propertyQName value from.
   * @param normalizer The qualified name normalizer to normalize the resulting propertyQName value.
   * @return An rdf literal as an rdf node.
   */
  @Override
  public RDFObject getValue(SQLResultTuple tuple, QNameNormalizer normalizer) {
    // Generate propertyQName value
    RDFObject object = null;
    String value = D2rUtil.parsePattern(getPattern(),
        D2R.PATTERN_DELIMINATOR, tuple);

    // The lang tag specifies indirectly the dataType (rdf:langeString)
    // Thus the lang tag has a higher priority than the dataType tag
    if ((value != null) && (getLangTag() != null)) {
      object = primitiveValueFactory.createLiteral(value, getLangTag());
    } else if ((value != null) && (getDataType() != null)) {
      // if no lang tag is set but the dataType tag createDoc a typed literal
      String dataType = normalizer.normalize(this.dataType);
      object = primitiveValueFactory.createTypedLiteral(value, dataType);
    }  else {
      // no lang tag and dataType set; assume xsd:string is the data type
      object = primitiveValueFactory.createLiteral(value);
    }
    return object;
  }
}