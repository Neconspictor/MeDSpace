package de.unipassau.medspace.d2r.exception;

/**
 * Generic D2R exception class.
 */
public class D2RException
    extends java.lang.Exception {

  /**
   * Creates a new D2RException object.
   * @param message The error message.
   */
  public D2RException(String message) {
    super(message);
  }

  /**
   * Creates a new D2RException object.
   * @param message The error message.
   * @param cause The parent cause.
   */
  public D2RException(String message, Throwable cause) {
    super(message, cause);
  }
}