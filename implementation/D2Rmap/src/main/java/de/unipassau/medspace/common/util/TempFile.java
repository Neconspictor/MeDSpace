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

  private static Logger log = LoggerFactory.getLogger(TempFile.class);

  private File source;

  public TempFile(String fileName, String suffix) throws IOException {
    source = File.createTempFile(fileName, suffix);
    source.deleteOnExit();
  }

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