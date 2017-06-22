package de.unipassau.medsapce.indexing;

/**
 * Created by David Goeth on 22.06.2017.
 */
public class IndexTuple {
  private String field;
  private String value;

  public IndexTuple(String field, String value) {
    this.field = field;
    this.value = value;
  }

  public String getField() {
    return field;
  }

  public String getValue() {
    return value;
  }
}
