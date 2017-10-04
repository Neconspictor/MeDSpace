package de.unipassau.medspace.common.stream;

import java.io.Closeable;
import java.io.IOException;

/**
 * The purpose of a StartCloseValidator is to act as an utility class for objects/resources that can be started and
 * closed and need validation of its started/closed state.
 */
public class StartCloseValidator implements Closeable {

  /**
   * Flag that states whether the validator has been started.
   */
  protected  boolean started;

  /**
   * Flag that states whether the validator has been closed.
   */
  protected  boolean isClosed;

  /**
   * Default constructor.
   * The validator is initialized to not started and not closed.
   */
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

  /**
   * Validates the closed status. After the validation check is performed, the validation state is changed to 'closed'.
   * As a result, calling this method a second time will result in throwing an IOException.
   * @throws IOException thrown if the validator is not started yet or already closed.
   */
  public void close() throws IOException {
    if (!started) throw new IOException("StartCloseValidator hasn't started yet!");
    if (isClosed) throw new IOException("StartCloseValidator is already closed!");
    isClosed = true;
  }

  /**
   * Starts the validator. Changes the state of the validator from 'not started' to 'opened'.
   * @throws IOException thrown if the state of the validator isn't 'not started'
   */
  public void start() throws IOException {
    if (isClosed) throw new IOException("StartCloseValidator is already closed!");
    if(started) throw new IOException("StartCloseValidator has already started!");
    started = true;
  }

  /**
   * Validates that the validator has started.
   * @throws IOException thrown if the validator hasn't started.
   */
  public void validateStarted() throws IOException {
    if (!started) throw new IOException("StartCloseValidator has not started!");
  }

  /**
   * Validates that the current state of the validator is 'opened'. The state is opened, if
   * the validator has been started but has not been closed, yet.
   * @throws IOException If the validator isn't open.
   */
  public void validateOpened() throws IOException {
    if (!isOpen()) throw new IOException("StartCloseValidator isn't open!");
  }
}