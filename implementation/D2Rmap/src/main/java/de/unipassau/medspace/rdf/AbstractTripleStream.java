package de.unipassau.medspace.rdf;

import org.apache.jena.graph.Triple;
import java.util.Iterator;

/**
 * Created by David Goeth on 29.06.2017.
 */
public abstract class AbstractTripleStream implements TripleStream {

  protected boolean started;
  protected boolean isClosed;

  public AbstractTripleStream() {
    started = false;
    isClosed = false;
  }

  @Override
  public boolean isOpen() {
    return !isClosed && started;
  }

  @Override
  public Iterator<Triple> iterator() {
    return this;
  }
}