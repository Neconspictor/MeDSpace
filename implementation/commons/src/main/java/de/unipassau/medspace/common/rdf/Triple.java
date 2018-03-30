package de.unipassau.medspace.common.rdf;

/**
 * Represents an RDF triple statement.
 */
public interface Triple {

  /**
   * Provides the subject.
   * @return the subject.
   */
  String getSubject();

  /**
   * Provides the predicate.
   * @return the predicate.
   */
  String getPredicate();

  /**
   * Provides the object.
   * @return the object.
   */
  String getObject();
}