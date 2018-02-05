package de.unipassau.medspace.wrapper.image_wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO
 */
public class LogWrapperInputStream extends InputStream {

  /**
   * TODO
   */
  private static Logger log = LoggerFactory.getLogger(LogWrapperInputStream.class);

  /**
   * TODO
   */
  protected InputStream source;

  /**
   * TODO
   * @param in
   */
  public LogWrapperInputStream(InputStream in) {
    this.source = in;
  }

  @Override
  public int read() throws IOException {
    try {
      return source.read();
    } catch (Throwable t) {
      log.error("Exception catched from source. Exception will be rethrown", t);
      throw t;
    }
  }

  @Override
  public void close() throws IOException {
    try {
      source.close();
    } catch (Throwable t) {
      log.error("Exception catched from source. Exception will be rethrown", t);
      throw t;
    }
  }
}
