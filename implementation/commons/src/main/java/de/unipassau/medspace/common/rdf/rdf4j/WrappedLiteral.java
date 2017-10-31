package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFLiteral;
import org.eclipse.rdf4j.model.Literal;

/**
 * Created by David Goeth on 31.10.2017.
 */
public class WrappedLiteral extends WrappedObject implements RDFLiteral {

  public WrappedLiteral(Literal literal) {
    super(literal);
  }

  public Literal get() {
    return (Literal) object;
  }
}