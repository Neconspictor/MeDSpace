package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.SimpleTriple;
import de.unipassau.medspace.common.rdf.Triple;
import org.eclipse.rdf4j.model.Statement;

/**
 * Created by David Goeth on 28.10.2017.
 */
public class TripleFactory {

  public static Triple create(Statement stmt) {
    final String subject = stmt.getSubject().toString();
    final String predicate = stmt.getPredicate().toString();
    final String object = stmt.getObject().toString();
    return new SimpleTriple(subject, predicate, object);
  }
}