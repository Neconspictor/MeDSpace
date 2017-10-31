package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFValue;
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
    return null;
  }

  @Override
  public String getPredicate() {
    return null;
  }

  @Override
  public RDFValue getObject() {
    return null;
  }

  public Statement getStatement() {
    return statement;
  }
}