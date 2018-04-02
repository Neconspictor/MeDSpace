package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.Triple;
import org.eclipse.rdf4j.model.Statement;

/**
 * A wrapper for RDF4J statements.
 */
public class WrappedStatement implements Triple {

  private final Statement statement;

  /**
   *  Creates a new WrappedStatement object.
   * @param statement The RDF4J statement to wrap.
   */
  public WrappedStatement(Statement statement) {
    this.statement = statement;
  }

  @Override
  public String getSubject() {
    return statement.getSubject().toString();
  }

  @Override
  public String getPredicate() {
    return statement.getPredicate().toString();
  }

  @Override
  public String getObject() {
    return statement.getObject().toString();
  }

  /**
   * Provides the wrapped statement.
   * @return the wrapped statement.
   */
  public Statement getStatement() {
    return statement;
  }
}