package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFObject;
import org.eclipse.rdf4j.model.Value;

/**
 * A wrapper for RDF4J values.
 */
public class WrappedObject implements RDFObject {

  /**
   * The wrapped RDF4J value
   */
  protected final Value object;

  /**
   * Creates a new WrappedObject object.
   * @param object The RDF4J value to wrap.
   */
  public WrappedObject(Value object) {
    this.object = object;
  }


  /**
   * Provides the RDF4J value.
   * @return the RDF4J value.
   */
  public Value get() {
    return object;
  }
}