package de.unipassau.medspace.common.util;

import java.util.Collection;

/**
 * Utility methods that cannot be assiged to a specific topic.
 */
public class MiskUtil {

  /**
   * Provides the first found element of a certain type from a collection.
   * @param collection The collection.
   * @param clazz The class of the type to search the element for.
   * @param <T> The type to search the element for.
   * @return The first found element or null if the collection doesn't have any element of that type.
   */
  public static <T> T getByClass(Collection<?> collection, Class<T> clazz) {
    for (Object elem : collection) {
      if (clazz.isInstance(elem)) return (T) elem;
    }
    return null;
  }
}