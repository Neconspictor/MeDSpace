package de.unipassau.medspace.common.util;

/**
 * A object converter creates a new object of type B from one source object of type A.
 */
public interface Converter<A, B> {

  /**
   * Converts object 'source' from type A to one object of type 'B'
   * @param source The object to convert
   * @return The converted object.
   */
  B convert(A source);
}
