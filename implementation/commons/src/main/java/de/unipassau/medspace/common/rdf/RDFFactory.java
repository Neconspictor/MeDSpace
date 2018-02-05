package de.unipassau.medspace.common.rdf;

/**
 * TODO
 */
public interface RDFFactory {

  /**
   * TODO
   * @param label
   * @return
   */
  RDFLiteral createLiteral(String label);

  /**
   * TODO
   * @param label
   * @param language
   * @return
   */
  RDFLiteral createLiteral(String label, String language);

  /**
   * TODO
   * @param iri
   * @return
   */
  RDFResource createResource(String iri);

  /**
   * TODO
   * @param label
   * @param dataType
   * @return
   */
  RDFLiteral createTypedLiteral(String label, String dataType);

  /**
   * TODO
   * @param subject
   * @param predicate
   * @param object
   * @return
   */
  Triple createTriple(RDFResource subject, RDFResource predicate, RDFObject object);
}