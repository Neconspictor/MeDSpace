package de.unipassau.medspace.common.rdf;

/**
 * Created by David Goeth on 31.10.2017.
 */
public class RDFValue {
  private final String value;
  private final boolean isResource;
  private String langTag;
  private String dataType;


  public RDFValue(String value, boolean isResource) {
    this.value = value;
    this.isResource = isResource;
    langTag = null;
    dataType = null;
  }

  public RDFValue addLangTag(String langTag) {
    RDFValue result = new RDFValue(value, isResource);
    result.dataType = this.dataType;
    result.langTag = langTag;
    return result;
  }

  public RDFValue addDataType(String dataType) {
    RDFValue result = new RDFValue(value, isResource);
    result.langTag = this.langTag;
    result.dataType = dataType;
    return result;
  }

  public String getValue() {
    return value;
  }

  public boolean isResource() {
    return isResource;
  }

  public String getLangTag() {
    return langTag;
  }

  public String getDataType() {
    return dataType;
  }
}