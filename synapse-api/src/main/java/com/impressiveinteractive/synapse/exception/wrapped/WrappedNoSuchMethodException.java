package com.impressiveinteractive.synapse.exception.wrapped;

/**
 * {@link WrappedException} for the {@link NoSuchMethodException}.
 */
public class WrappedNoSuchMethodException extends WrappedReflectiveOperationException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedNoSuchMethodException(NoSuchMethodException e) {
        super(e);
    }

    @Override
    public synchronized NoSuchMethodException getCause() {
        return (NoSuchMethodException) super.getCause();
    }

    @Override
    public void unwrap() throws NoSuchMethodException {
        throw getCause();
    }
}
