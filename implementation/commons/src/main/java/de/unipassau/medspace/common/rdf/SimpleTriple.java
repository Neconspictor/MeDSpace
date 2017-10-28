package de.unipassau.medspace.common.rdf;

/**
 * Created by David Goeth on 28.10.2017.
 */
public class SimpleTriple implements Triple {

  private final String subject;
  private final String predicate;
  private final String object;

  public SimpleTriple(String subject, String predicate, String object) {
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
    return object;
  }
}