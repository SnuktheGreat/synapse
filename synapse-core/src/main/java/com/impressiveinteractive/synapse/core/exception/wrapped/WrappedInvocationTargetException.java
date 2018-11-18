package com.impressiveinteractive.synapse.core.exception.wrapped;

import java.lang.reflect.InvocationTargetException;

/**
 * {@link WrappedException} for the {@link InvocationTargetException}.
 */
public class WrappedInvocationTargetException extends WrappedReflectiveOperationException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedInvocationTargetException(InvocationTargetException e) {
        super(e);
    }

    @Override
    public synchronized InvocationTargetException getCause() {
        return (InvocationTargetException) super.getCause();
    }

    @Override
    public void unwrap() throws InvocationTargetException {
        throw getCause();
    }
}
