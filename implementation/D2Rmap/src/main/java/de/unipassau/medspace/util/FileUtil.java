package de.unipassau.medspace.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for handling files, directories and resources.
 */
public class FileUtil {

  private static Logger log = Logger.getLogger(FileUtil.class);

  public static void closeSilently(Closeable closeable) {
    closeSilently(closeable, true);
  }

  public static void closeSilently(Closeable closeable, boolean logErrors) {
    if (closeable == null) return;
    try {
      closeable.close();
      System.out.println("closed...");
    } catch (IOException e) {
      // just ignore;
      if (logErrors)
        log.error(e);
    }
  }

  public static Path createDirectory(String directory) throws IOException {
    assert directory != null;

    Path path = null;

    try {
      path = Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IOException("specified directory path isn't a valid path: " + directory);
    }

    File dir = new File(directory);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new IOException("Couldn't create directory from: " + path);
      }
    }

    if (!dir.isDirectory()) {
      throw new IOException("Specified path isn't a directory: " + path);
    }

    return path;
  }

  /**
   * Checks whether a given string represents a valid resource.
   * @param resourceName the path to the resource
   * @return true if <b>resourceName</b> is a valid resource, otherwise false.
   */
  public static boolean isResource(String resourceName) {
    if (resourceName == null) throw new NullPointerException("Null isn't allowed for resourceName");
     return  Class.class.getResource(resourceName) != null;
  }

  /**
   * Provides the absolute file path to a given resource.
   * @param resourceName The resource path.
   * @return The absolute file path to the resource.
   * @throws IllegalArgumentException if <b>resourceName</b> isn't a valid resource.
   * @throws NullPointerException if <b>resourceName</b> is null
   */
  public static String getAbsoluteFilePathFromResource(String resourceName) {
    if (resourceName == null) throw new NullPointerException("resourceName isn't allowed to be null");
    if (!isResource(resourceName)) throw new IllegalArgumentException("resourceName describes no valid resource!");
    URL resource = Class.class.getResource(resourceName);
    return resource.getFile();
  }

  public static void write(InputStream in, String output) throws IOException {
    FileOutputStream file = new FileOutputStream(output);
    byte[] buffer = new byte[1024];

    try {
      while (in.available() > 0) {
        int currentReadedBytes = in.read(buffer, 0, buffer.length);
        file.write(buffer, 0, currentReadedBytes);
      }
    } catch (IOException e) {
      throw e;
    } finally {
      closeSilently(file);
    }
  }
}