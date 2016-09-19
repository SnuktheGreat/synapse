package com.impressiveinteractive.synapse.exception.runtime;

/**
 * Runtime variation of the {@link IllegalAccessException}.
 */
public class RuntimeIllegalAccessException extends RuntimeReflectiveOperationException {

    /**
     * Creates empty exception.
     */
    public RuntimeIllegalAccessException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeIllegalAccessException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeIllegalAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeIllegalAccessException(Throwable cause) {
        super(cause);
    }
}
