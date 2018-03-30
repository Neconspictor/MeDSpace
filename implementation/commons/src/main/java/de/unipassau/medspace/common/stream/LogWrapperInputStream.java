package de.unipassau.medspace.common.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that wraps another input stream in order to log events.
 */
public class LogWrapperInputStream extends InputStream {

  private static Logger log = LoggerFactory.getLogger(LogWrapperInputStream.class);

  /**
   * The wrapped input stream.
   */
  protected InputStream source;

  /**
   * Creates a new LogWrapperInputStream object.
   * @param in The input stream to wrap.
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
