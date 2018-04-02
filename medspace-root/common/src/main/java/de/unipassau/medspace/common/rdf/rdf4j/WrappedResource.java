package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.RDFResource;
import org.eclipse.rdf4j.model.IRI;

/**
 * A wrapper for RDF4J IRIs.
 */
public class WrappedResource extends WrappedObject implements RDFResource {


  /**
   * Creates a new WrappedResource object.
   * @param resource The IRI to wrap.
   */
  public WrappedResource(IRI resource) {
    super(resource);
  }

  @Override
  public IRI get() {
    return (IRI) object;
  }
}