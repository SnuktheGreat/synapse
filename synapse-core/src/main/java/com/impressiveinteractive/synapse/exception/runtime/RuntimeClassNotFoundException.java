package com.impressiveinteractive.synapse.exception.runtime;

/**
 * Runtime variation of the {@link ClassNotFoundException}.
 */
public class RuntimeClassNotFoundException extends RuntimeReflectiveOperationException {

    /**
     * Creates empty exception.
     */
    public RuntimeClassNotFoundException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeClassNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeClassNotFoundException(Throwable cause) {
        super(cause);
    }
}
