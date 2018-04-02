package de.unipassau.medspace.common.rdf.mapping;

/**
 * An identifiable is an object that has an ID which can be represented as a string value.
 */
public interface Identifiable {

  /**
   * Provides the ID.
   * @return The ID.
   */
  String getId();
}