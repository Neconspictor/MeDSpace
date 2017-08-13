package de.unipassau.medspace.common.rdf;

/**
 * Defines an rdf namespace with prefix and URI.
 */
public class Namespace {

  /**
   * The URI of the namespace.
   */
  private String fullURI;

  /**
   * The prefix that proxies the namespace.
   */
  private String prefix;

  /**
   * Constructs a new namespace with a given prefix and URI.
   * @param prefix The prefix for the namespace.
   * @param fullURI The URI of the namespace.
   */
  public Namespace(String prefix, String fullURI) {
    this.prefix = prefix;
    this.fullURI = fullURI;
  }

  /**
   * Provides the URI of the namespace.
   * @return The URI.
   */
  public String getFullURI() {
    return fullURI;
  }

  /**
   * Provides the prefix of the namespace.
   * @return The prefix.
   */
  public String getPrefix() {
    return prefix;
  }
}