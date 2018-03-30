package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFLiteral;
import org.eclipse.rdf4j.model.Literal;

/**
 * A wrapper for RDF4J literals.
 */
public class WrappedLiteral extends WrappedObject implements RDFLiteral {

  /**
   * Creates a new WrappedLiteral object.
   * @param literal The RDF4J literal that should be wrapped.
   */
  public WrappedLiteral(Literal literal) {
    super(literal);
  }

  @Override
  public Literal get() {
    return (Literal) object;
  }
}