package de.fuberlin.wiwiss.d2r.exception;

/**
 * Generic D2R exception class.
 * <BR>History: 01-15-2003   : Initial version of this class.
 * <BR>History: 09-25-2003   : Changed for Jena2.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class D2RException
    extends java.lang.Exception {
  private String message = null;

  public D2RException(String message) {
    this.message = message;
  }

  public D2RException(String message, Throwable cause) {

    super(message, cause);
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }
}