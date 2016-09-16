package com.impressiveinteractive.synapse.exception;

import java.io.IOException;

public class RuntimeIOException extends WrappedCheckedException {

    public RuntimeIOException(IOException e) {
        super(e);
    }

    public RuntimeIOException(IOException e, String message, Object... args) {
        super(e, message, args);
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
