package de.unipassau.medspace.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * A temp file is a file designed as a work sheet for the application. A temporary file has usually only a
 * short life time, but at the latest it is automatically deleted when the JVM exists.
 */
public class TempFile implements Closeable {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(TempFile.class);

  /**
   * The used file.
   */
  private File source;

  /**
   * Creates a new TempFile.
   * @param fileName The filename of this temporary file.
   * @param suffix The file ending of this temporary file.
   * @throws IOException thrown if any IO-Error occurs.
   */
  public TempFile(String fileName, String suffix) throws IOException {
    source = File.createTempFile(fileName, suffix);
    source.deleteOnExit();
  }

  /**
   * Provides the file this class holds.
   * @return
   */
  public File get() {
    return source;
  };

  @Override
  public void close() throws IOException {
    if (!source.delete()) {
      throw new IOException("Couldn't delete temporary file: " + source.getAbsolutePath());
    }

    if (log.isDebugEnabled())
      log.debug("TempFile successfully deleted: " + source.getAbsolutePath());
  }
}