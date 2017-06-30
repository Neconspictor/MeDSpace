package de.unipassau.medspace.common.stream;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class StartCloseValidator {
  protected  boolean started;
  protected  boolean isClosed;

  public StartCloseValidator() {
    started = false;
    isClosed = false;
  }

  /**
   * Checks if this {@code StartCloseValidator} is open. A {@code StartCloseValidator} is open, if it has started but
   * isn't closed yet.
   * @return true if this {@code StartCloseValidator} is open;
   */
  public boolean isOpen() {
    return !isClosed && started;
  }

  public void validateClose() throws IOException {
    if (!started) throw new IllegalStateException("StartCloseValidator hasn't started yet!");
    if (isClosed) throw new IllegalStateException("StartCloseValidator is already closed!");
    isClosed = true;
  }

  public void validateHasNext() {
    if (!started) throw new IllegalStateException("StartCloseValidator is closed!");
  }

  public void validateNext() {
    if (!isOpen()) throw new IllegalStateException("StartCloseValidator isn't open!");
  }

  public void validateStart() throws IOException {
    if (isClosed) throw new IllegalStateException("StartCloseValidator is already closed!");
    if(started) throw new IllegalStateException("StartCloseValidator has already started!");
    started = true;
  }
}