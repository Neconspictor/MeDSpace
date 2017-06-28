package de.unipassau.medsapce.rdf;

import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class ResourceStream implements Closeable, Iterable<Triple> {

  @Override
  public void close() throws IOException {

  }

  @Override
  public Iterator<Triple> iterator() {
    return null;
  }

  @Override
  public void forEach(Consumer<? super Triple> action) {

  }

  @Override
  public Spliterator<Triple> spliterator() {
    return null;
  }
}
