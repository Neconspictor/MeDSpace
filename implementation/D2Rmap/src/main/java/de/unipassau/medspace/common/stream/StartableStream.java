package de.unipassau.medspace.common.stream;

import java.io.IOException;

/**
 * A {@code StartableStream} is a source or destination of data that isn't initialized on object creation but if its {@link StartableStream#start}
 * method is called. <br><br>
 * A StartableStream should allocate resources not during the constructor call/object creation but within the
 * forementioned validateStart method. Therefore, the constructor of a {@code StartableStream} usually shouldn't throw
 * any Exception. The constructor should perform fast and shouldn't perform any blocking operations. <p/>
 */
public interface StartableStream<E> extends DataSourceStream<E>{

  /**
   * Starts this stream
   */
  void start() throws IOException;
}
