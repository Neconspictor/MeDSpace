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

  /**
   * Specifies the rdf data type of the properties created by this bridge
   */
  protected String dataType;

  /**
   * An optional rdf language tag created properties should have.
   */
  protected String langTag;

  /**
   * TODO
   * @param primitiveValueFactory
   */
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

  /**
   * Provides the used datatype (if any is used) or null otherwise.
   * @return The used datatype or null if no datatype is used.
   */
  public String getDataType() {
    return this.dataType;
  }

  /**
   * Provides the language tag, the values of created properties should assigned to.
   * @return the language tag.
   */
  public String getLangTag() {
    return langTag;
  }

  /**
   * Sets the rdf data type created propertyQName values should have.
   * @param dataType The wished rdf data type.
   */
  public void setDataType(String dataType) {
    if (dataType == null) {
      this.dataType = null;
      return;
    }

    this.dataType = dataType.trim();
    if (this.dataType.equals("")) {
      this.dataType = null;
    }
  }

  /**
   * Sets the rdf language tag, created propertyQName values should assigned to.
   * @param langTag The wished language tag
   */
  public void setLangTag(String langTag) {
    if (langTag == null) {
      this.langTag = null;
      return;
    }

    this.langTag = langTag.trim();
    if (this.langTag.equals("")) {
      this.langTag = null;
    }
  }
}