package com.impressiveinteractive.synapse.exception;

public class RuntimeInterruptedException extends WrappedCheckedException {

    public RuntimeInterruptedException(InterruptedException e) {
        super(e);
    }

    public RuntimeInterruptedException(InterruptedException e, String message, Object... args) {
        super(e, message, args);
    }

    @Override
    public synchronized InterruptedException getCause() {
        return (InterruptedException) super.getCause();
    }

    @Override
    public void unwrap() throws InterruptedException {
        throw getCause();
    }
}
