package com.impressiveinteractive.synapse.exception.runtime;

/**
 * Runtime variation of the {@link ReflectiveOperationException}.
 */
public class RuntimeReflectiveOperationException extends RuntimeException {

    /**
     * Creates empty exception.
     */
    public RuntimeReflectiveOperationException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeReflectiveOperationException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeReflectiveOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeReflectiveOperationException(Throwable cause) {
        super(cause);
    }
}
