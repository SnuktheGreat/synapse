package com.impressiveinteractive.synapse.core.exception.runtime;

/**
 * Runtime variation of the {@link java.lang.reflect.InvocationTargetException}.
 */
public class RuntimeInvocationTargetException extends RuntimeReflectiveOperationException {

    /**
     * Creates empty exception.
     */
    public RuntimeInvocationTargetException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeInvocationTargetException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeInvocationTargetException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeInvocationTargetException(Throwable cause) {
        super(cause);
    }
}
