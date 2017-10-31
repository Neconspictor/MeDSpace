package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFObject;
import org.eclipse.rdf4j.model.Value;

/**
 * Created by David Goeth on 31.10.2017.
 */
public class WrappedObject implements RDFObject {

  protected final Value object;

  public WrappedObject(Value object) {
    this.object = object;
  }

  public Value get() {
    return object;
  }
}