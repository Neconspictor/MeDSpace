package de.unipassau.medspace.common.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Ian Pojman <pojman@gmail.com>
 */
public abstract class LookaheadIterator<T> implements Iterator<T> {
  /** The predetermined "validateNext" object retrieved from the wrapped iterator, can be null. */
  protected T next;

  /**
   * Implement the validateHasNext policy of this iterator.
   * Returns true of the getNext() policy returns a new item.
   */
  public boolean hasNext()
  {
    if (next != null)
    {
      return true;
    }

    // we havent done it already, so go find the validateNext thing...
    if (!doesHaveNext())
    {
      return false;
    }

    return getNext();
  }

  /** by default we can return true, since our logic does not rely on validateHasNext() - it prefetches the validateNext */
  protected boolean doesHaveNext() {
    return true;
  }

  /**
   * Fetch the validateNext item
   * @return false if the validateNext item is null.
   */
  protected boolean getNext()
  {
    next = loadNext();

    return next!=null;
  }

  /**
   * Subclasses implement the 'get validateNext item' functionality by implementing this method. Implementations return null when they have no more.
   * @return Null if there is no validateNext.
   */
  protected abstract T loadNext();

  /**
   * Return the validateNext item from the wrapped iterator.
   */
  public T next()
  {
    if (!hasNext())
    {
      throw new NoSuchElementException();
    }

    T result = next;

    next = null;

    return result;
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}