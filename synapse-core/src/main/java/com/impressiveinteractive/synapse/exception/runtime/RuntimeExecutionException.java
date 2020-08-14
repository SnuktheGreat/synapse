package com.impressiveinteractive.synapse.exception.runtime;

import com.impressiveinteractive.synapse.exception.ExceptionalCallable;
import com.impressiveinteractive.synapse.exception.ExceptionalRunnable;
import com.impressiveinteractive.synapse.lambda.SerializableFunction;

import java.util.concurrent.ExecutionException;

public class RuntimeExecutionException extends RuntimeException {

    public static <V> V wrap(ExceptionalCallable<V, ExecutionException> callable) {
        try {
            return callable.call();
        } catch (ExecutionException e) {
            throw new RuntimeExecutionException(e);
        }
    }

    public static void wrap(ExceptionalRunnable<ExecutionException> runnable) {
        try {
            runnable.run();
        } catch (ExecutionException e) {
            throw new RuntimeExecutionException(e);
        }
    }

    public RuntimeExecutionException() {
    }

    public RuntimeExecutionException(String message) {
        super(message);
    }

    public RuntimeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeExecutionException(Throwable cause) {
        super(cause);
    }
}
