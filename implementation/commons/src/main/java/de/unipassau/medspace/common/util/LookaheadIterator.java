package de.unipassau.medspace.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A LookaheadIterator pre-fetches data in order to decide if data does exists or not. <br><br>
 *
 * This class is based on the version from Ian Pojman (pojman@gmail.com), but has been adapted and some additional
 * documentation has been added, too.
 *
 * @author Ian Pojman (pojman@gmail.com), David Goeth (goeth@fim.uni-passau.de) added documentation, removed not needed
 * stuff
 */
public abstract class LookaheadIterator<T> implements Iterator<T> {
  /**
   * The predetermined "next" item. Null indicates, that it hasn't been loaded, yet.
   * */
  protected T next;

  /**
   * Returns true if the next item has been prefetched or otherwise fetches a new item and returns true if
   * the result isn't null.
   */
  public boolean hasNext()
  {
    if (next != null) {
      return true;
    }

    return getNext();
  }

  /**
   * Fetches the next item
   * @return false if the next item is null.
   */
  protected boolean getNext() {
    next = loadNext();
    return next != null;
  }

  /**
   * Subclasses implement the 'get next item' functionality by implementing this method. Implementations
   * return null when they have no more items to provide.
   * @return Null if there is no next item.
   */
  protected abstract T loadNext();

  /**
   * Return the next item from the wrapped iterator.
   */
  public T next()
  {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    T result = next;
    next = null;
    return result;
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException always
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}