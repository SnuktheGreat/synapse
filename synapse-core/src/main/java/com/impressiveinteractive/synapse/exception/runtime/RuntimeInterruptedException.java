package com.impressiveinteractive.synapse.exception.runtime;

import com.impressiveinteractive.synapse.exception.BiExceptionalCallable;
import com.impressiveinteractive.synapse.exception.BiExceptionalRunnable;
import com.impressiveinteractive.synapse.exception.ExceptionalCallable;
import com.impressiveinteractive.synapse.exception.ExceptionalRunnable;

public final class RuntimeInterruptedException extends RuntimeException {

    public static <V, E2 extends Exception> V biWrap(BiExceptionalCallable<V, InterruptedException, E2> callable) throws E2 {
        try {
            return callable.call();
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.interruptAndCreate(e);
        }
    }

    public static <E2 extends Exception> void biWrap(BiExceptionalRunnable<InterruptedException, E2> callable) throws E2 {
        try {
            callable.run();
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.interruptAndCreate(e);
        }
    }

    public static <V> V wrap(ExceptionalCallable<V, InterruptedException> callable) {
        try {
            return callable.call();
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.interruptAndCreate(e);
        }
    }

    public static void wrap(ExceptionalRunnable<InterruptedException> runnable) {
        try {
            runnable.run();
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.interruptAndCreate(e);
        }
    }

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
