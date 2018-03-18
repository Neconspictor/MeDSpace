package de.unipassau.medspace.common.util;

import java.util.Collection;

/**
 * TODO
 */
public class MiskUtil {

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