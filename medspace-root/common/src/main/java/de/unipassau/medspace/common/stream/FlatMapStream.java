package de.unipassau.medspace.common.stream;

import java.io.Closeable;
import java.io.IOException;

/**
 * A flat map stream maps a 'stream of a stream of type T' to 'a stream of type T'.
 */
public class FlatMapStream<T> implements Stream<T> {

  private final Stream<Stream<T>> source;

  private Stream<T> current;

  /**
   * Creates a new FlatMapStream object.
   * @param source The stream to flat map.
   */
  public FlatMapStream(Stream<Stream<T>> source) {
    this.source = source;
  }


  @Override
  public T next() throws IOException {
    if (!hasNext()) throw new IOException("No next element available!");

    return current.next();
  }

  @Override
  public boolean hasNext() throws IOException {

    if (current == null || !current.hasNext()) {
      current = fetchNewCollection();
    }

    if (current == null) return false;

    return current.hasNext();
  }

  private Stream<T> fetchNewCollection() throws IOException {
    if (!source.hasNext()) return null;
    return source.next();
  }

  @Override
  public void close() throws IOException {
    try {
      if (current != null) current.close();
      source.close();
    } finally {
      closeSilently(current);
      closeSilently(source);
    }
  }

  private void closeSilently(Closeable closeable) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (IOException e) {}
  }
}