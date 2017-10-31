package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFValue;
import de.unipassau.medspace.common.rdf.Triple;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Created by David Goeth on 29.10.2017.
 */
public class StatementFactory {

  protected static final ValueFactory factory = SimpleValueFactory.getInstance();

  public Statement createFromWrapped(Triple triple) {

    if (! (triple instanceof WrappedStatement)) {
      throw new IllegalArgumentException("Triple has to be of type " + WrappedStatement.class);
    }

    WrappedStatement wrapped = (WrappedStatement) triple;

    return wrapped.getStatement();
  }
}