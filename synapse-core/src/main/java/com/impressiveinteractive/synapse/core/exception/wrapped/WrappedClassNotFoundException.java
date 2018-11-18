package com.impressiveinteractive.synapse.core.exception.wrapped;

/**
 * {@link WrappedException} for the {@link ClassNotFoundException}.
 */
public class WrappedClassNotFoundException extends WrappedReflectiveOperationException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedClassNotFoundException(ClassNotFoundException e) {
        super(e);
    }

    @Override
    public synchronized ClassNotFoundException getCause() {
        return (ClassNotFoundException) super.getCause();
    }

    @Override
    public void unwrap() throws ClassNotFoundException {
        throw getCause();
    }
}
