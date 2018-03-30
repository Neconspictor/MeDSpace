package de.unipassau.medspace.common.rdf;

/**
 * An RDF value can be anything from an RDF resource or RDF literal. Blank nodes are not supported.
 */
public class RDFValue {
  private final String value;
  private final boolean isResource;
  private String langTag;
  private String dataType;


  /**
   * Creates a new RDFValue object.
   * @param value The string value.
   * @param isResource Is this RDF value a resource?
   */
  public RDFValue(String value, boolean isResource) {
    this.value = value;
    this.isResource = isResource;
    langTag = null;
    dataType = null;
  }

  /**
   * Creates a copy of this object and adds an RDF language tag to the created object.
   * @param langTag The RDF language tag
   * @return a new RDF value that has a language tag.
   */
  public RDFValue addLangTag(String langTag) {
    RDFValue result = new RDFValue(value, isResource);
    result.dataType = this.dataType;
    result.langTag = langTag;
    return result;
  }

  /**
   * Creates a copy of this object and adds an RDF data type.
   * @param dataType The data type.
   * @return a new RDF value with a data type.
   */
  public RDFValue addDataType(String dataType) {
    RDFValue result = new RDFValue(value, isResource);
    result.langTag = this.langTag;
    result.dataType = dataType;
    return result;
  }

  /**
   * Provides the value.
   * @return the value.
   */
  public String getValue() {
    return value;
  }

  /**
   * Checks if this RDF value is a resource.
   * @return true if this RDF value is a resource.
   */
  public boolean isResource() {
    return isResource;
  }

  /**
   * Provides the language tag of this RDF value.
   * @return the language tag
   */
  public String getLangTag() {
    return langTag;
  }

  /**
   * Provides the data type of this RDF value.
   * @return the data type
   */
  public String getDataType() {
    return dataType;
  }
}