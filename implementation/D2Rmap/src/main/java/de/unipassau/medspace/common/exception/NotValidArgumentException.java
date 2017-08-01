package de.unipassau.medspace.common.exception;

/**
 * Created by David Goeth on 01.08.2017.
 */
public class NotValidArgumentException extends Exception {
private String message = null;

public NotValidArgumentException(String message) {
    this.message = message;
    }

public NotValidArgumentException(String message, Throwable cause) {
    super(message, cause);
    this.message = message;
    }

public String getMessage() {
    return this.message;
    }
}