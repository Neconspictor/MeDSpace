package de.unipassau.medspace.common.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * TODO
 */
public class URLConverter {

  /**
   * TODO
   * @param value
   * @return
   * @throws MalformedURLException
   */
  public static URL parseURL(String value) throws MalformedURLException {
    return new URL(value);
  }

  /**
   * TODO
   * @param value
   * @return
   */
  public static String printURL(URL value) {
    return value.toString();
  }
}