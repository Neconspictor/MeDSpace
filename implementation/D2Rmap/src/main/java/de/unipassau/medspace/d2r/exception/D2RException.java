package de.unipassau.medspace.d2r.exception;

/**
 * Generic D2R exception class.
 */
public class D2RException
    extends java.lang.Exception {

  public D2RException(String message) {
    super(message);
  }

  public D2RException(String message, Throwable cause) {
    super(message, cause);
  }
}