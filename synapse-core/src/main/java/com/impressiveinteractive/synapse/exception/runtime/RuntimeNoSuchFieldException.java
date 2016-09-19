package com.impressiveinteractive.synapse.exception.runtime;

/**
 * Runtime variation of the {@link NoSuchFieldException}.
 */
public class RuntimeNoSuchFieldException extends RuntimeReflectiveOperationException {

    /**
     * Creates empty exception.
     */
    public RuntimeNoSuchFieldException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeNoSuchFieldException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeNoSuchFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeNoSuchFieldException(Throwable cause) {
        super(cause);
    }
}
