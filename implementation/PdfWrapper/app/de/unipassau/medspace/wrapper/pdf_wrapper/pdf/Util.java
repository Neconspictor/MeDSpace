package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;

import java.util.*;

/**
 * TODO
 */
public class Util {

  /**
   * TODO
   * @param collection
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> T getByClass(Collection<?> collection, Class<T> clazz) {
    for (Object elem : collection) {
      if (clazz.isInstance(elem)) return (T) elem;
    }
    return null;
  }
}