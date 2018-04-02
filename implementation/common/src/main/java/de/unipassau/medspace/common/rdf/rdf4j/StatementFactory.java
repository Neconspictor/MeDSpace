package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.Triple;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Used to create RDF4J statements from MeDSpace RDF triples.
 */
public class StatementFactory {

  /**
   * The used value factory.
   */
  protected static final ValueFactory factory = SimpleValueFactory.getInstance();

  /**
   * Converts a MeDSpace RDF triple to a RDF4J statement.
   * @param triple The triple to convert.
   * @return The converted RDF4J statement.
   */
  public Statement createFromWrapped(Triple triple) {

    if (! (triple instanceof WrappedStatement)) {
      throw new IllegalArgumentException("Triple has to be of type " + WrappedStatement.class);
    }

    WrappedStatement wrapped = (WrappedStatement) triple;

    return wrapped.getStatement();
  }
}