package de.unipassau.medspace.common.rdf;

/**
 * Created by David Goeth on 31.10.2017.
 */
public interface RDFFactory {

  RDFLiteral createLiteral(String label);

  RDFLiteral createLiteral(String label, String language);

  RDFResource createResource(String iri);

  RDFLiteral createTypedLiteral(String label, String dataType);

  Triple createTriple(RDFResource subject, RDFResource predicate, RDFObject object);
}