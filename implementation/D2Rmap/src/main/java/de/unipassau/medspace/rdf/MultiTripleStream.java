package de.unipassau.medspace.rdf;

import de.unipassau.medspace.util.FileUtil;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class MultiTripleStream extends AbstractTripleStream {

  private static Logger log = Logger.getLogger(MultiTripleStream.class);

  private Queue<TripleStream> streams;

  public MultiTripleStream() {
    super();
    this.streams = new LinkedList<>();
  }

  public MultiTripleStream(Queue<TripleStream> streams) {
    super();
    this.streams = streams;
  }


  public void add(TripleStream stream) {
    //if (started) throw new IllegalStateException("MultiTripleStream has started yet!");
    if (isClosed) throw new IllegalStateException("MultiTripleStream is already closed!");
    streams.add(stream);
  }

  public void addAll(Collection<? extends TripleStream> coll) {
    //if (started) throw new IllegalStateException("MultiTripleStream has started yet!");
    if (isClosed) throw new IllegalStateException("MultiTripleStream is already closed!");
    streams.addAll(coll);
  }

  @Override
  public void close() throws IOException {
    if (!started) throw new IllegalStateException("MultiTripleStream has not started yet!");
    if (isClosed) throw new IllegalStateException("MultiTripleStream is already closed!");

    isClosed = true;
    for (TripleStream stream : streams) {
      if (stream.isOpen()) FileUtil.closeSilently(stream, true);
    }

    // we don't need anymore the TripleStreams, so release them
    streams.clear();
    log.debug("Successfully closed.");
  }

  @Override
  public boolean hasNext() {
    if (!isOpen()) throw new IllegalStateException("MultiTripleStream is not open!");
    TripleStream activeStream = getActiveStream();
    return activeStream != null;
  }

  @Override
  public Triple next() {
    if (!isOpen()) throw new IllegalStateException("MultiTripleStream is not open!");
    TripleStream activeStream = getActiveStream();
    if (activeStream == null) throw new IllegalStateException("No next Triple available!");
    return activeStream.next();
  }

  @Override
  public void start() throws IOException {
    if (started) throw new IllegalStateException("MultiTripleStream has already started!");
    if (isClosed) throw new IllegalStateException("MultiTripleStream is already closed!");

    started = true;
    if (getActiveStream() == null) {
      log.warn("MultiStripleStream has started streaming, but no streams were added!");
    }
  }

  /**
   * Provides the current active triple stream, that is open and has triples to fetch.
   * If the first stream is not open, it will be started.
   * If the first stream has no triples to provide, it will be closed and removed from the stream list.
   * Than the next (first) available stream will be checked. If no stream could be found that has triples to offer, null
   * will be returned indicating that this MultiTripleStream has no triples more.
   *
   * @return The current active triple stream to fetch triples from or null, if no stream is available more.
   */
  private TripleStream getActiveStream() {

    while(!streams.isEmpty()) {

      TripleStream stream = streams.peek();

      // assume, that a not open stream hasn't been started yet
      // and is not closed yet
      if (!stream.isOpen()) {
        try {
          stream.start();
        } catch (IOException e) {
          log.error("Error while trying to start a stream: ", e);

          // Just remove the stream and get the next
          // We don't care about errors occuring while closing it
          FileUtil.closeSilently(stream, false);
          streams.poll();
          continue;
        }
      }

      if (!stream.hasNext()) {

        // Just remove the stream and get the next
        // Errors shouldn't occur normally, so log them
        FileUtil.closeSilently(stream, true);
        streams.poll();
        continue;
      }

      return stream;
    }

    return null;
  }
}