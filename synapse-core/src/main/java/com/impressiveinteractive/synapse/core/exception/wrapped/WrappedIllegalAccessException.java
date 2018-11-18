package com.impressiveinteractive.synapse.core.exception.wrapped;

/**
 * {@link WrappedException} for the {@link IllegalAccessException}.
 */
public class WrappedIllegalAccessException extends WrappedReflectiveOperationException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedIllegalAccessException(IllegalAccessException e) {
        super(e);
    }

    @Override
    public synchronized IllegalAccessException getCause() {
        return (IllegalAccessException) super.getCause();
    }

    @Override
    public void unwrap() throws IllegalAccessException {
        throw getCause();
    }
}
