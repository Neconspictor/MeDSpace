package de.unipassau.medspace.common.rdf;

/**
 * URI
 */
public class Namespace {

  private String fullURI;
  private String prefix;

  public Namespace(String prefix, String fullURI) {
    this.prefix = prefix;
    this.fullURI = fullURI;
  }

  public String getFullURI() {
    return fullURI;
  }

  public String getPrefix() {
    return prefix;
  }
}
