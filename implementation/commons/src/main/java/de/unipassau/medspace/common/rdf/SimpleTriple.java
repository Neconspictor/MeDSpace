package de.unipassau.medspace.common.rdf;

/**
 * A simple RDF triple.
 */
public class SimpleTriple implements Triple {

  private final String subject;
  private final String predicate;
  private final RDFValue object;

  /**
   * Creates a new SimpleTriple object.
   * @param subject the subject
   * @param predicate the predicate
   * @param object the object
   */
  public SimpleTriple(String subject, String predicate, RDFValue object) {
    this.subject = subject;
    this.predicate = predicate;
    this.object = object;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public String getPredicate() {
    return predicate;
  }

  @Override
  public String getObject() {
    return object.toString();
  }
}