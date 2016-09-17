package com.impressiveinteractive.synapse.exception.runtime;

/**
 * Runtime variation of the {@link java.io.IOException}.
 */
public class RuntimeIOException extends RuntimeException {

    /**
     * Creates empty exception.
     */
    public RuntimeIOException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeIOException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeIOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeIOException(Throwable cause) {
        super(cause);
    }
}
