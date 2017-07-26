package de.unipassau.medspace.common.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class StreamProxy<E> implements StartableStream<E> {

  private static Logger log = LoggerFactory.getLogger(StreamProxy.class);

  protected StreamFactory<E> factory;
  protected DataSourceStream<E> impl;
  protected StartCloseValidator validator;


  public StreamProxy(StreamFactory<E> factory) {
    validator = new StartCloseValidator();
    impl = null;
    this.factory = factory;

  }

  @Override
  public void close() throws IOException {
    validator.validateClose();
    impl.close();
  }

  @Override
  public boolean hasNext() {
    try {
      validator.validateHasNext();
    } catch (IOException e) {
      log.error("Error during validation", e);
      return false;
    }
    return impl.iterator().hasNext();
  }

  /**
   * Checks if this {@code StartableStream} is open. A {@code StartableStream} is open, if it has started but isn't closed yet.
   * @return true if this {@code StartableStream} is open;
   */
  public boolean isOpen() {
    return validator.isOpen();
  }

  @Override
  public E next() {
    try {
      validator.validateNext();
    } catch (IOException e) {
      log.error("Error during validation", e);
      return null;
    }
    return impl.iterator().next();
  }

  @Override
  public void start() throws IOException {

    validator.validateStart();
    assert impl == null;

    impl = factory.create();
    if (impl instanceof StartableStream) {
      ((StartableStream<E>) impl).start();
    }
  }
}