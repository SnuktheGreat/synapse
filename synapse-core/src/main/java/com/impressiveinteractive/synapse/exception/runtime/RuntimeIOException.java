package com.impressiveinteractive.synapse.exception.runtime;

import com.impressiveinteractive.synapse.exception.BiExceptionalCallable;
import com.impressiveinteractive.synapse.exception.ExceptionalCallable;
import com.impressiveinteractive.synapse.exception.ExceptionalRunnable;

import java.io.IOException;

/**
 * Runtime variation of the {@link java.io.IOException}.
 */
public class RuntimeIOException extends RuntimeException {

    public static <V, E2 extends Exception> V biWrap(BiExceptionalCallable<V, IOException, E2> callable) throws E2 {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static <V> V wrap(ExceptionalCallable<V, IOException> callable) {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void wrap(ExceptionalRunnable<IOException> runnable) {
        try {
            runnable.run();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    /**
     * Creates empty exception.
     */
    public RuntimeIOException() {
        // noop
    }

    /**
     * Creates exception with given message.
     *
     * @param message Given message.
     */
    public RuntimeIOException(String message) {
        super(message);
    }

    /**
     * Creates exception with given message and cause.
     *
     * @param message Given message.
     * @param cause   Given cause.
     */
    public RuntimeIOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception with the given cause.
     *
     * @param cause Given cause.
     */
    public RuntimeIOException(Throwable cause) {
        super(cause);
    }
}
