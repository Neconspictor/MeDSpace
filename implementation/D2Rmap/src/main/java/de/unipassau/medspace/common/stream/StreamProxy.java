package de.unipassau.medspace.common.stream;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class StreamProxy<E> implements StartableStream<E> {

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
    validator.validateHasNext();
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
    validator.validateNext();
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