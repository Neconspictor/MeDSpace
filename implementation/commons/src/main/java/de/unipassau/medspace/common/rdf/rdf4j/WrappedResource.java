package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFResource;
import org.eclipse.rdf4j.model.IRI;

/**
 * Created by David Goeth on 31.10.2017.
 */
public class WrappedResource extends WrappedObject implements RDFResource {


  public WrappedResource(IRI resource) {
    super(resource);
  }

  public IRI get() {
    return (IRI) object;
  }
}