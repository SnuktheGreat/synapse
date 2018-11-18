package com.impressiveinteractive.synapse.core.exception.runtime;

/**
 * Runtime variation of the {@link InstantiationException}.
 */
public class RuntimeInstantiationException extends RuntimeReflectiveOperationException {

    /**
     * Creates empty exception.
     */
    public RuntimeInstantiationException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeInstantiationException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeInstantiationException(Throwable cause) {
        super(cause);
    }
}
