package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.Triple;
import org.eclipse.rdf4j.model.Statement;

/**
 * Created by David Goeth on 31.10.2017.
 */
public class WrappedStatement implements Triple {

  private final Statement statement;

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

  public Statement getStatement() {
    return statement;
  }
}