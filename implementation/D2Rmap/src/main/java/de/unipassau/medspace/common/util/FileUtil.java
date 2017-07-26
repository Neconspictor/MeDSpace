package de.unipassau.medspace.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Utility class for handling files, directories and resources.
 */
public class FileUtil {

  private static Logger log = LoggerFactory.getLogger(FileUtil.class);

  public static void closeSilently(AutoCloseable closeable) {
    closeSilently(closeable, true);
  }

  public static void closeSilently(AutoCloseable closeable, boolean logErrors) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (Exception e) {
      // just ignore;
      if (logErrors)
        log.error("Error while closing Closable", e);
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

  public static URL getResource(String filename) {
    return FileUtil.class.getResource(filename);
  }

  /**
   * Checks whether a given string represents a valid resource.
   * @param resourceName the path to the resource
   * @return true if <b>resourceName</b> is a valid resource, otherwise false.
   */
  public static boolean isResource(String resourceName) {
    if (resourceName == null) throw new NullPointerException("Null isn't allowed for resourceName");
    URL url = getResource(resourceName);
    if (url == null) return false;
    File file = new File(url.getFile());
    return !file.isDirectory();
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
    URL resource = FileUtil.class.getResource(resourceName);
    FileUtil.class.getResource(resourceName);
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

  public static TempFile createTempFileFromResource(String resourceName, String tempFileName)
      throws IOException {

    TempFile file = null;
    URL res = FileUtil.class.getResource(resourceName);

    InputStream input = null;
    OutputStream out = null;

    try {
      if (res.toString().startsWith("jar:")) {
        input = FileUtil.class.getResourceAsStream(resourceName);
        file = new TempFile(new Date().getTime() + " " + tempFileName, "tmp");
        out = new FileOutputStream(file.get());
        int read;
        byte[] bytes = new byte[1024];

        while ((read = input.read(bytes)) != -1) {
          out.write(bytes, 0, read);
        }
        out.flush();

      } else {
        //this will probably work in your IDE, but not from a JAR
        file = new TempFile(res.getFile(), "tmp");
      }
    } catch (IOException ex) {
      throw new IOException("Couldn't create temporary file from resource: " + resourceName, ex);
    } finally {
      closeSilently(out);
      closeSilently(input);
    }

    return file;
  }
}