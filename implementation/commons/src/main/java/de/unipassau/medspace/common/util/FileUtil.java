package de.unipassau.medspace.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for handling files, directories and resources.
 */
public class FileUtil {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(FileUtil.class);

  /**
   * Closes an {@link AutoCloseable} but catches any exceptions thrown while closing it. The catched exceptions
   * will be logged.
   * @param closeable Will be closed.
   */
  public static void closeSilently(AutoCloseable closeable) {
    closeSilently(closeable, true);
  }

  /**
   * Closes an {@link AutoCloseable} but catches any exceptions thrown while closing it.
   * @param closeable Will be closed.
   * @param logErrors Specifies if catched exceptions should be logged.
   */
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

  /**
   * Creates a directory structure from a given directory path and returns a path to that directory.
   * @param directory Represents a directory
   * @return A path representing the directory.
   * @throws IOException If the specified directory string doesn't represent a directory or another IO-Error occurs.
   */
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
        throw new IOException("Couldn't createDoc directory from: " + path);
      }
    }

    if (!dir.isDirectory()) {
      throw new IOException("Specified path isn't a directory: " + path);
    }

    return path;
  }

  /**
   * Creates a temporary file from a resource.
   * @param resourceName The name of the resource.
   * @param tempFileName The wished name of the created temporary file.
   * @return The temporary file having the same content as the resource.
   * @throws IOException If an IO-Error occurs.
   */
  public static TempFile createTempFileFromResource(String resourceName, String tempFileName)
      throws IOException {

    TempFile file = null;
    InputStream input = null;
    OutputStream out = null;

    try {

      input = FileUtil.class.getResourceAsStream(resourceName);
      file = new TempFile(new Date().getTime() + " " + tempFileName, ".tmp");
      out = new FileOutputStream(file.get());
      int read;
      byte[] bytes = new byte[1024];

      while ((read = input.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }
      out.flush();

    } catch (IOException ex) {
      throw new IOException("Couldn't createDoc temporary file from resource: " + resourceName, ex);
    } finally {
      closeSilently(out);
      closeSilently(input);
    }

    return file;
  }


  /**
   * TODO
   * @param file
   * @return
   * @throws IOException
   */
  public static List<String> getLineContent(File file) throws IOException {
    List<String> result = new LinkedList<>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    try {
      String line = reader.readLine();
      while(line != null) {
        result.add(line);
        line = reader.readLine();
      }
    } finally {
      reader.close();
    }

    return result;
  }

  /**
   * Provides a URL from a given resource file name.
   * @param filename The resource to get an URL from.
   * @return The URL to the specified resource.
   */
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

  /**
   * TODO
   * @param root
   * @param subFile
   * @return
   * @throws IOException
   */
  public static String getRelativePath(File root, File subFile)  {

    if (!root.isDirectory())
      throw new IllegalArgumentException("root isn't a directory!");

    String canonicalRoot = makePlatformIndependentPathStructure(root);
    String canonicalSubFile = makePlatformIndependentPathStructure(subFile);

    if (!canonicalSubFile.startsWith(canonicalRoot)) {
      throw new IllegalArgumentException("File to get the relative path from isn't a sub file from root!");
    }

    int length = canonicalRoot.length();

    // we create a substring from canonicalSubFile by subtracting canonicalRoot
    // But we have to assure that the resulting string doesn't start with a path separator.
    if (!canonicalRoot.endsWith("/")) {
      ++length;
    }
    return canonicalSubFile.substring(length, canonicalSubFile.length());
  }

  /**
   * TODO
   * @param file
   * @return
   */
  public static String makePlatformIndependentPathStructure(File file) {
    String canonicalRoot = file.getAbsolutePath();

    //use only '/' as it is supported by all platforms
    return canonicalRoot.replaceAll("\\\\", "/");
  }
}