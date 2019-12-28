package com.impressiveinteractive.synapse.exception.runtime;

public final class RuntimeInterruptedException extends RuntimeException {

    public static RuntimeInterruptedException interruptAndCreate() {
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException();
    }

    public static RuntimeInterruptedException interruptAndCreate(String message) {
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException(message);
    }

    public static RuntimeInterruptedException interruptAndCreate(String message, Throwable cause) {
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException(message, cause);
    }

    public static RuntimeInterruptedException interruptAndCreate(Throwable cause) {
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException(cause);
    }

    private RuntimeInterruptedException() {
    }

    private RuntimeInterruptedException(String message) {
        super(message);
    }

    private RuntimeInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    private RuntimeInterruptedException(Throwable cause) {
        super(cause);
    }
}
