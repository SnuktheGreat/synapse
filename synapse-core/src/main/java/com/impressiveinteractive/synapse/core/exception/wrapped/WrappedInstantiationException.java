package com.impressiveinteractive.synapse.core.exception.wrapped;

/**
 * {@link WrappedException} for the {@link InstantiationException}.
 */
public class WrappedInstantiationException extends WrappedReflectiveOperationException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedInstantiationException(InstantiationException e) {
        super(e);
    }

    @Override
    public synchronized InstantiationException getCause() {
        return (InstantiationException) super.getCause();
    }

    @Override
    public void unwrap() throws InstantiationException {
        throw getCause();
    }
}
