package de.unipassau.medspace.common.exception;
/**
 * A checked exception to specify that an error has occurred while parsing a file.
 */
public class ParseException extends Exception {

  /**
   * Constructs a new ParseException from a given error message.
   * @param message The error message to use for the exception cause.
   */
  public ParseException(String message) {
    super(message);
  }

  /**
   * Constructs a new ParseException from a given error message and cause.
   * @param message The error message to use.
   * @param cause The cause of the rise of this exception.
   */
  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }
}