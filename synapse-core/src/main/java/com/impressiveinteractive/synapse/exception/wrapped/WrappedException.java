package com.impressiveinteractive.synapse.exception.wrapped;

/**
 * This class is a {@link RuntimeException} designed to wrap checked exceptions. It is recommended to use the most
 * specific subclass of this exception, in order to keep a similar type hierarchy to the original exception. A more
 * specific subclass should also have a more specific {@link #unwrap()} signature.
 * <p>
 * This class was designed to be used in conjunction with the {@code wrap} methods in
 * {@link com.impressiveinteractive.synapse.exception.Exceptions}
 */
public class WrappedException extends RuntimeException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception.
     */
    public WrappedException(Exception e) {
        super(e);
    }

    @Override
    public synchronized Exception getCause() {
        return (Exception) super.getCause();
    }

    /**
     * Rethrow the causing (unchecked) exception.
     *
     * @throws Exception The causing (unchecked) exception.
     */
    public void unwrap() throws Exception {
        throw getCause();
    }
}
