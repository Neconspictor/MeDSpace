package de.unipassau.medspace.common.stream;

import de.unipassau.medspace.common.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A stream collection is a startable stream that combines a couple of other streams to one stream.
 * The base idea of a stream collection is to process the underlying streams in sequence.
 * If one stream has no data more, the next stream is processed until all streams have no data more.
 */
public class StreamCollection<E> implements StartableStream<E> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(StreamCollection.class);

  /**
   * The underlying streams to combine to one stream.
   */
  private Queue<StreamProxy<E>> streams;

  /**
   * Used in methods that need some start/open/stop validation
   */
  private StartCloseValidator validator;

  /**
   * Flag to specifiy whether this class should rethrow exceptions
   * that are thrown by the currently active stream or whether the exception
   * should be ignored and skip the stream's data and process instead the next stream.
   */
  private boolean rethrowExceptions;

  /**
   * Constructs a new StreamCollection
   */
  public StreamCollection() {
    super();
    this.streams = new LinkedList<>();
    validator = new StartCloseValidator();
    rethrowExceptions = false;
  }

  /**
   * Constructs a new StreamCollection and adds a queue of stream factories. The stream factories are used to create or
   * provide streams that should be combined by this StreamCollection.
   * @param streams The stream factories that should be added to this StreamCollection
   */
  public StreamCollection(Queue<StreamFactory<E>> streams) {
    this();
    addAll(streams);
  }


  /**
   * Adds a StreamFactory to this stream collection.
   * This class will use the factory class to create a stream from it.
   * @param factory The StreamFactory to add.
   */
  public void add(StreamFactory<E> factory) {
    streams.add(new StreamProxy<E>(factory));
  }

  /**
   * Adds a collection of StreamFactory objects to this class.
   * This class will use the factory objects to create streams from it.
   * @param coll The collection of StreamFactory objects to add.
   */
  public void addAll(Collection<? extends StreamFactory<E>> coll) {
    for (StreamFactory<E> factory : coll) {
      add(factory);
    }
  }

  @Override
  public void close() throws IOException {
    validator.close();
    for (StreamProxy<E> stream : streams) {
      if (stream.isOpen()) FileUtil.closeSilently(stream, true);
    }

    // we don't need anymore the TripleStreams, so release them
    streams.clear();
    log.debug("Successfully closed.");
  }

  @Override
  public boolean hasNext() throws IOException {
    try {
      validator.validateStarted();
    } catch (IOException e) {
      log.error("Error while validation", e);
      return false;
    }
    StreamProxy<E> activeStream = getActiveStream();
    return activeStream != null;
  }

  @Override
  public E next() throws IOException {
    validator.validateOpened();
    StreamProxy<E> activeStream = getActiveStream();
    if (activeStream == null) throw new IllegalStateException("No valid next object available!");
    return activeStream.next();
  }

  @Override
  public void start() throws IOException {
    validator.start();
    if (getActiveStream() == null) {
      log.warn("StreamCollection has started streaming, but no streams were added!");
    }
  }

  /**
   * Provides the current active stream, that is open and has triples to fetch.
   * If the first stream is not open, it will be started.
   * If the first stream has no triples to provide, it will be closed and removed from the stream list.
   * Than the validateOpened (first) available stream will be checked. If no stream could be found that has triples to offer, null
   * will be returned indicating that this stream has no more data to provide.
   *
   * @return The current active stream to fetch data from or null, if no stream is available more.
   */
  private StreamProxy<E> getActiveStream() throws IOException {

    while(!streams.isEmpty()) {

      StreamProxy<E> stream = streams.peek();

      // assume, that a not open stream hasn't been started yet
      // and is not closed yet
      if (!stream.isOpen()) {
        try {
          stream.start();
        } catch (IOException e) {
          log.error("Error while trying to start a stream: ", e);

          if (rethrowExceptions) throw e;

          // Just remove the stream and get the validateOpened
          // We don't care about errors occuring while closing it
          FileUtil.closeSilently(stream, false);
          streams.poll();
          continue;
        }
      }

      if (!stream.hasNext()) {

        // Just remove the stream and get the validateOpened
        // Errors shouldn't occur normally, so log them
        FileUtil.closeSilently(stream, true);
        streams.poll();
        continue;
      }

      return stream;
    }

    return null;
  }

  /**
   * Sets a flag to sepcify if this class should rethrow exceptions thrown by processed streams or whether
   * it should ignore the exceptions and skip the current active stream and proceed to the next stream.
   * @param rethrowExceptions true if exceptions should be rethrown or ignored if set to false.
   */
  public void setRethrowExceptions(boolean rethrowExceptions) {
    this.rethrowExceptions = rethrowExceptions;
  }
}