package de.unipassau.medspace.register;

/**
 * Created by David Goeth on 01.10.2017.
 */
public class Test {
  private final Builder builder;

  public int getField1() {
    return builder.field1;
  }
  public String getField2() {
    return builder.field2;
  }

  /**
   * Private constructor, as instances should only be allowed by using a builder.
   */
  private Test(Builder builder) {

    // create a copy of the mutable builder so that
    // it can only be accessed by this class.
    this.builder = new Builder(builder);
  }

  public static class Builder {

    private int field1;
    private String field2;

    public Builder() {
      field1 = 0;
      field2 = null;
    }

    /**
     * This is a copy constructor: It creates a deep copy of the given Builder object 'other'.
     * @param other The Builder used to initialize this object.
     */
    public Builder(Builder other) {
     field1 = other.field1;
     field2 = other.field2;
    }

    Test build() {
      return new Test(this);
    }

    void setField1(int field1) {
      this.field1 = field1;
    }
    void setField2(String field2) {
      this.field2 = field2;
    }
  }
}