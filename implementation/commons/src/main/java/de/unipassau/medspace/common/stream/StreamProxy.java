package de.unipassau.medspace.common.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A stream proxy is a startable stream, that proxies another stream or startable stream.
 */
public class StreamProxy<E> implements StartableStream<E> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(StreamProxy.class);

  /**
   * The stream factory which is used to create the proxied stream.
   */
  protected StreamFactory<E> factory;

  /**
   * The proxied stream.
   */
  protected Stream<E> impl;

  /**
   * A validator used in methods that needs validation.
   */
  protected StartCloseValidator validator;


  /**
   * Creates a new stream proxy from a given stream factory.
   * @param factory The factory that is used by this class to produce the
   *        proxied stream.
   */
  public StreamProxy(StreamFactory<E> factory) {
    validator = new StartCloseValidator();
    impl = null;
    this.factory = factory;

  }

  @Override
  public void close() throws IOException {
    validator.close();
    impl.close();
  }

  @Override
  public boolean hasNext() throws IOException {
    validator.validateStarted();
    return impl.hasNext();
  }

  /**
   * Checks if this {@code StartableStream} is open. A {@code StartableStream} is open, if it has started
   * but isn't closed yet.
   * @return true if this {@code StartableStream} is open;
   */
  public boolean isOpen() {
    return validator.isOpen();
  }

  @Override
  public E next() throws IOException {
    validator.validateOpened();
    return impl.next();
  }

  @Override
  public void start() throws IOException {

    validator.start();
    assert impl == null;

    impl = factory.create();
    if (impl instanceof StartableStream) {
      ((StartableStream<E>) impl).start();
    }
  }
}