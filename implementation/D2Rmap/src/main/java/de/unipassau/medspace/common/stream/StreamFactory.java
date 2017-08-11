package de.unipassau.medspace.common.stream;

import java.io.IOException;

/**
 * A factory interface for streams of a certain type.
 */
public interface StreamFactory<E> {

  /**
   * Creates a new stream.
   * @return A new stream of a set of data.
   * @throws IOException thrown if the stream couldn't be created properly.
   */
  Stream<E> create() throws IOException;
}
