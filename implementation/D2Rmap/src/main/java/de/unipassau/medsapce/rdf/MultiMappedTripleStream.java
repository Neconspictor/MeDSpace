package de.unipassau.medsapce.rdf;

import de.unipassau.medspace.util.FileUtil;
import org.apache.jena.graph.Triple;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class MultiMappedTripleStream implements TripleStream {

  private LinkedList<TripleStream> streams;
  private volatile boolean isClosed;

  public MultiMappedTripleStream(LinkedList<TripleStream> streams) {
    this.streams = streams;
    isClosed = false;
  }

  @Override
  public void close() throws IOException {
    isClosed = true;
    for (TripleStream stream : streams) {
      FileUtil.closeSilently(stream, true);
    }
  }

  @Override
  public Iterator<Triple> iterator() {
    return this;
  }

  @Override
  public void forEach(Consumer<? super Triple> action) {
    forEachRemaining(action);
  }

  /**
   * Not implemented
   * @throws UnsupportedOperationException
   */
  @Override
  public Spliterator<Triple> spliterator() {
    throw new UnsupportedOperationException("spliterator() is not implemented!");
  }

  @Override
  public boolean hasNext() {
    if (isClosed) throw new IllegalStateException("MultiMappedTripleStream is already closed!");
    TripleStream activeStream = getActiveStream();
    return activeStream != null;
  }

  @Override
  public Triple next() {
    if (isClosed) throw new IllegalStateException("MultiMappedTripleStream is already closed!");
    TripleStream activeStream = getActiveStream();
    if (activeStream == null) throw new IllegalStateException("No next Triple available!");
    return activeStream.next();
  }

  /**
   * Not implemented
   * @throws UnsupportedOperationException
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove() is not implemented!");
  }

  @Override
  public void forEachRemaining(Consumer<? super Triple> action) {
    action.accept(next());
  }

  private TripleStream getActiveStream() {
    while(!streams.isEmpty()) {
      TripleStream stream = streams.peekFirst();
      if (!stream.hasNext()) {
        FileUtil.closeSilently(stream, true);
        streams.removeFirst();
      } else {
        return stream;
      }
    }
    return null;
  }
}