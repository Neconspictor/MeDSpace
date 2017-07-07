package de.unipassau.medspace.common.stream;

import de.unipassau.medspace.common.util.FileUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by David Goeth on 28.06.2017.
 */
public class StreamCollection<E> implements StartableStream<E> {

  private static Logger log = Logger.getLogger(StreamCollection.class);

  private Queue<StreamProxy<E>> streams;
  private StartCloseValidator validator;

  public StreamCollection() {
    super();
    this.streams = new LinkedList<>();
    validator = new StartCloseValidator();
  }

  public StreamCollection(Queue<StreamFactory<E>> streams) {
    this();
    addAll(streams);
  }


  public void add(StreamFactory<E> factory) {
    //if (started) throw new IllegalStateException("StreamCollection has started yet!");
    //if (isClosed) throw new IllegalStateException("StreamCollection is already closed!");
    streams.add(new StreamProxy<E>(factory));
  }

  public void addAll(Collection<? extends StreamFactory<E>> coll) {
    //if (started) throw new IllegalStateException("StreamCollection has started yet!");
    //if (isClosed) throw new IllegalStateException("StreamCollection is already closed!");
    for (StreamFactory<E> factory : coll) {
      add(factory);
    }
  }

  @Override
  public void close() throws IOException {
    validator.validateClose();
    for (StreamProxy<E> stream : streams) {
      if (stream.isOpen()) FileUtil.closeSilently(stream, true);
    }

    // we don't need anymore the TripleStreams, so release them
    streams.clear();
    log.debug("Successfully closed.");
  }

  @Override
  public boolean hasNext() {
    try {
      validator.validateHasNext();
    } catch (IOException e) {
      log.error(e);
      return false;
    }
    StreamProxy<E> activeStream = getActiveStream();
    return activeStream != null;
  }

  @Override
  public E next() {
    try {
      validator.validateNext();
    } catch (IOException e) {
      log.error(e);
      return null;
    }

    StreamProxy<E> activeStream = getActiveStream();
    if (activeStream == null) throw new IllegalStateException("No valid next object available!");
    return activeStream.iterator().next();
  }

  @Override
  public void start() throws IOException {
    validator.validateStart();
    if (getActiveStream() == null) {
      log.warn("StreamCollection has started streaming, but no streams were added!");
    }
  }

  /**
   * Provides the current active stream, that is open and has triples to fetch.
   * If the first stream is not open, it will be started.
   * If the first stream has no triples to provide, it will be closed and removed from the stream list.
   * Than the validateNext (first) available stream will be checked. If no stream could be found that has triples to offer, null
   * will be returned indicating that this stream has no more data to provide.
   *
   * @return The current active stream to fetch data from or null, if no stream is available more.
   */
  private StreamProxy<E> getActiveStream() {

    while(!streams.isEmpty()) {

      StreamProxy<E> stream = streams.peek();

      // assume, that a not open stream hasn't been started yet
      // and is not closed yet
      if (!stream.isOpen()) {
        try {
          stream.start();
        } catch (IOException e) {
          log.error("Error while trying to start a stream: ", e);

          // Just remove the stream and get the validateNext
          // We don't care about errors occuring while closing it
          FileUtil.closeSilently(stream, false);
          streams.poll();
          continue;
        }
      }

      if (!stream.iterator().hasNext()) {

        // Just remove the stream and get the validateNext
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