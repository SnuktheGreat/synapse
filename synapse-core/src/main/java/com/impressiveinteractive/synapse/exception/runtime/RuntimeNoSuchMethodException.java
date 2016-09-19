package com.impressiveinteractive.synapse.exception.runtime;

/**
 * Runtime variation of the {@link NoSuchMethodException}.
 */
public class RuntimeNoSuchMethodException extends RuntimeReflectiveOperationException {

    /**
     * Creates empty exception.
     */
    public RuntimeNoSuchMethodException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeNoSuchMethodException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeNoSuchMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeNoSuchMethodException(Throwable cause) {
        super(cause);
    }
}
