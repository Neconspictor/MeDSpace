package de.unipassau.medspace.common.stream;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Created by David Goeth on 30.06.2017.
 */
public interface DataSourceStream<E> extends Closeable, Iterable<E>, Iterator<E> {

  @Override
  default Iterator<E> iterator() {
    return this;
  }
}
