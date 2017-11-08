package de.unipassau.medspace.common.exception;

/**
 * A checked exception to specify that a given argument isn't valid.
 */
public class NoValidArgumentException extends Exception {

    /**
     * Constructs a new NoValidArgumentException from a given error message.
     * @param message The error message to use for the exception cause.
     */
    public NoValidArgumentException(String message) {
    super(message);
    }

    /**
     * Constructs a new NoValidArgumentException from a given error message and cause.
     * @param message The error message to use.
     * @param cause The cause of the rise of this exception.
     */
    public NoValidArgumentException(String message, Throwable cause) {
    super(message, cause);
    }
}