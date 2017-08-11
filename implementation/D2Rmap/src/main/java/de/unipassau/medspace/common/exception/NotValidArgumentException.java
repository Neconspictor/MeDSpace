package de.unipassau.medspace.common.exception;

/**
 * A checked exception to specify that a given argument isn't valid.
 */
public class NotValidArgumentException extends Exception {

    /**
     * Constructs a new NotValidArgumentException from a given error message.
     * @param message The error message to use for the exception cause.
     */
    public NotValidArgumentException(String message) {
    super(message);
    }

    /**
     * Constructs a new NotValidArgumentException from a given error message and cause.
     * @param message The error message to use.
     * @param cause The cause of the rise of this exception.
     */
    public NotValidArgumentException(String message, Throwable cause) {
    super(message, cause);
    }
}