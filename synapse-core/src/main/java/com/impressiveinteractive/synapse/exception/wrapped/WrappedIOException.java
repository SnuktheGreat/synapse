package com.impressiveinteractive.synapse.exception.wrapped;

import java.io.IOException;

/**
 * {@link WrappedException} for the {@link IOException}.
 */
public class WrappedIOException extends WrappedException {

    /**
     * Create a new runtime variant of the given exception.
     *
     * @param e Given exception
     */
    public WrappedIOException(IOException e) {
        super(e);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }

    @Override
    public void unwrap() throws IOException {
        throw getCause();
    }
}
