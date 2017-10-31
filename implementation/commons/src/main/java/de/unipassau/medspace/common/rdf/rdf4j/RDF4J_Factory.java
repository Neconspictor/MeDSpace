package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Created by David Goeth on 31.10.2017.
 */
public class RDF4J_Factory implements RDFFactory {

  private static final ValueFactory factory = SimpleValueFactory.getInstance();

  @Override
  public RDFLiteral createLiteral(String label) {
    Literal literal = factory.createLiteral(label);
    return new WrappedLiteral(literal);
  }

  @Override
  public RDFLiteral createLiteral(String label, String language) {
    Literal literal = factory.createLiteral(label, language);
    return new WrappedLiteral(literal);
  }

  @Override
  public RDFResource createResource(String iri) {
    IRI resource = factory.createIRI(iri);
    return new WrappedResource(resource);
  }

  @Override
  public RDFLiteral createTypedLiteral(String label, String dataType) {
    IRI dataTypeIRI = factory.createIRI(dataType);
    Literal literal = factory.createLiteral(label, dataTypeIRI);
    return new WrappedLiteral(literal);
  }

  @Override
  public Triple createTriple(RDFResource subject, RDFResource predicate, RDFObject object) {

    WrappedResource wrappedS = (WrappedResource) subject;
    WrappedResource wrappedP = (WrappedResource) predicate;
    WrappedObject wrappedO = (WrappedObject) object;

    if (wrappedS == null
        || wrappedP == null
        || wrappedO == null) {
      throw new IllegalArgumentException("One of the arguments tye isn't compatible with this rdf factory: "
      + subject.getClass() + ", " + predicate.getClass() + ", " + object.getClass());
    }

    IRI s = wrappedS.get();
    IRI p = wrappedP.get();
    Value o = wrappedO.get();

    Statement statement = factory.createStatement(s, p, o);

    return new WrappedStatement(statement);
  }
}