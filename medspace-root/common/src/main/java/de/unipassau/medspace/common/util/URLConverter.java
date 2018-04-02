package de.unipassau.medspace.common.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Converts Strings to URLs and URLs to strings.
 */
public class URLConverter {

  /**
   * Converts a string to a URL.
   * @param value The string to convert.
   * @return The converted URL.
   * @throws MalformedURLException If the string cannot be converted to a URL.
   */
  public static URL parseURL(String value) throws MalformedURLException {
    return new URL(value);
  }

  /**
   * Creates a string from a URL.
   * @param value The URL to convert.
   * @return The string of the URL.
   */
  public static String printURL(URL value) {
    return value.toString();
  }
}