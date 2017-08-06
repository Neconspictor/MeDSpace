package de.unipassau.medspace.d2r.bridge;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rUtil;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * D2r Bridge for properties with a data type (Literals).
 */
public class DatatypePropertyBridge extends Bridge {

  /**
   * Creates a property value from a given SQLResultTuple and normalizes it with the normalizer.
   * The resulting property value will be an rdf literal.
   * @param tuple The sql result tuple to get the propertyQName value from.
   * @param normalizer The qualified name normalizer to normalize the resulting propertyQName value.
   * @return An rdf literal as an rdf node.
   */
  @Override
  public RDFNode getValue(SQLResultTuple tuple, QNameNormalizer normalizer) {
    // Generate propertyQName value
    Literal literal = null;
    String value = D2rUtil.parsePattern(getPattern(),
        D2R.DELIMINATOR, tuple);

    // The lang tag specifies indirectly the dataType (rdf:langeString)
    // Thus the lang tag has a higher priority than the dataType tag
    if ((value != null) && (getLangTag() != null)) {
      literal = ResourceFactory.createLangLiteral((String) value, getLangTag());
    } else if ((value != null) && (getDataType() != null)) {
      // if no lang tag is set but the dataType tag createDoc a typed literal
      String dataType = normalizer.normalize(this.dataType);
      RDFDatatype rdfType = TypeMapper.getInstance().getSafeTypeByName(dataType);
      literal = ResourceFactory.createTypedLiteral(value, rdfType);
    }  else {
      // no lang tag and dataType set; assume xsd:string is the data type
      literal = ResourceFactory.createTypedLiteral(value);
    }
    return literal;
  }
}