package de.unipassau.medspace.common.stream;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by David Goeth on 30.06.2017.
 */
public interface DataSourceStream<E> extends Closeable {

  E next() throws IOException;
  boolean hasNext() throws IOException;
}
