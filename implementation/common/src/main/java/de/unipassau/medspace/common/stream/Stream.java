package de.unipassau.medspace.common.stream;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A Stream is an iterator trait over a set of data of a certain type. But in contrast to the
 * java.util.Iterator interface, all methods can throw an IOException. The additional exception
 * handling is useful for data sets that are created dynamically and involve IO-Operations and thus
 * can throw an exception. A common application this interface could be useful for, is fetching
 * data from a database or parsing java objects from a big file that cannot be loaded as a whole
 * into memory.
 */
public interface Stream<E> extends Closeable {

  /**
   * Provides the next element from this stream.
   * @return The next element of this stream.
   * @throws IOException thrown if no next element exists in the stream or another low-level
   * IO-Error occurs.
   */
  E next() throws IOException;

  /**
   * Checks, if the stream has more elements to provide.
   * @return true, if at least one element can be fetched from the stream.
   * @throws IOException thrown if a low-level IO-Error occurs.
   */
  boolean hasNext() throws IOException;
}