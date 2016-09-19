package com.impressiveinteractive.synapse.exception.wrapped;

/**
 * {@link WrappedException} for the {@link NoSuchFieldException}.
 */
public class WrappedNoSuchFieldException extends WrappedReflectiveOperationException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedNoSuchFieldException(NoSuchFieldException e) {
        super(e);
    }

    @Override
    public synchronized NoSuchFieldException getCause() {
        return (NoSuchFieldException) super.getCause();
    }

    @Override
    public void unwrap() throws NoSuchFieldException {
        throw getCause();
    }
}
