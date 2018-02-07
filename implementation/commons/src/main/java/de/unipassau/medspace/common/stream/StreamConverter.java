package de.unipassau.medspace.common.stream;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;

import java.io.IOException;

/**
 * A stream that converts a stream of type 'A' to a stream of type 'B'
 */
public class StreamConverter<A, B> implements Stream<B> {

  /**
   * Used as input for accessing the mapped sql tuples.
   */
  private final Stream<A> stream;

  /**
   * Used to convert the mapped sql tuples to the desired document class.
   */
  private final Converter<A, B> converter;

  /**
   * Creates a new SqlToDocStream.
   * @param stream Used as input for accessing the mapped sql tuples
   * @param converter Used to convert the mapped sql tuples to the desired document class.
   */
  public StreamConverter(Stream<A> stream,
                         Converter<A, B> converter) {
    this.stream = stream;
    this.converter = converter;
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  @Override
  public boolean hasNext() throws IOException {
    return stream.hasNext();
  }

  @Override
  public B next() throws IOException {
    A elem = stream.next();
    return converter.convert(elem);
  }
}