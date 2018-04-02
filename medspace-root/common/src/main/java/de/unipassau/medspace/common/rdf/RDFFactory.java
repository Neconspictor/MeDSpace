package de.unipassau.medspace.common.rdf;

/**
 * An RDF factory is used to create resources, literals, and triples.
 */
public interface RDFFactory {

  /**
   * Creates an RDF literal from a string value.
   * @param label The string value
   * @return An RDF literal representing the string value.
   */
  RDFLiteral createLiteral(String label);

  /**
   * Creates an RDF literal from a string value.
   * @param label The string value
   * @param language A language tag for the string value.
   * @return An RDF literal representing the string value.
   */
  RDFLiteral createLiteral(String label, String language);

  /**
   * Creates an RDF resource from an IRI.
   * @param iri The IRI
   * @return An RDF resource representing the IRI.
   */
  RDFResource createResource(String iri);

  /**
   * Creates an RDF literal from a string value that has a data type.
   * @param label The string value
   * @param dataType The data type
   * @return A typed RDF literal representing the string value.
   */
  RDFLiteral createTypedLiteral(String label, String dataType);

  /**
   * Creates an RDF triple.
   * @param subject The subject
   * @param predicate The predicate
   * @param object The object.
   * @return an RDF triple.
   */
  Triple createTriple(RDFResource subject, RDFResource predicate, RDFObject object);
}