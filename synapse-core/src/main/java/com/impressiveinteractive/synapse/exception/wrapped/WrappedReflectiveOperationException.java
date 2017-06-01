package com.impressiveinteractive.synapse.exception.wrapped;

/**
 * {@link WrappedException} for the {@link ReflectiveOperationException}.
 */
public class WrappedReflectiveOperationException extends WrappedException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception
     */
    public WrappedReflectiveOperationException(ReflectiveOperationException e) {
        super(e);
    }

    @Override
    public synchronized ReflectiveOperationException getCause() {
        return (ReflectiveOperationException) super.getCause();
    }

    @Override
    public void unwrap() throws ReflectiveOperationException {
        throw getCause();
    }
}
