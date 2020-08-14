package com.impressiveinteractive.synapse.exception;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface ExceptionalCallable<V, E extends Exception> extends Callable<V> {
    @Override
    V call() throws E;
}
