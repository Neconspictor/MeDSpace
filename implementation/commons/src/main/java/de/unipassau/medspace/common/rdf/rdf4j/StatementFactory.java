package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.Triple;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Created by David Goeth on 29.10.2017.
 */
public class StatementFactory {

  protected static final ValueFactory factory = SimpleValueFactory.getInstance();

  public Statement create(Triple triple) {
    Resource subject = factory.createIRI(triple.getSubject());
    IRI predicate = factory.createIRI(triple.getPredicate());
    Value object = (Value) () -> triple.getSubject();
    return factory.createStatement(subject, predicate, object);
  }
}