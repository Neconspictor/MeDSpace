package de.unipassau.medspace.common.exception;

/**
 * A checked exception to specify that a service isn't supported.
 */
public class UnsupportedServiceException extends Exception {

  /**
   * Constructs a new UnsupportedServiceException from a given error message.
   * @param message The error message to use for the exception cause.
   */
  public UnsupportedServiceException(String message) {
    super(message);
  }

  /**
   * Constructs a new UnsupportedServiceException from a given error message and cause.
   * @param message The error message to use.
   * @param cause The cause of the rise of this exception.
   */
  public UnsupportedServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
