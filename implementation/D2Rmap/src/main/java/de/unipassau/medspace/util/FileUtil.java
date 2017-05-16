package de.unipassau.medspace.util;


/**
 * Created by David Goeth on 15.05.2017.
 */
public class FileUtil {

  public static boolean isResource(String filename) {
     return  Class.class.getResource(filename) != null;
  }
}
