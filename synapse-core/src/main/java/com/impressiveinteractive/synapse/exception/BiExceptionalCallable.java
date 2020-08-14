package com.impressiveinteractive.synapse.exception;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface BiExceptionalCallable<V, E1 extends Exception, E2 extends Exception> extends Callable<V> {
    @Override
    V call() throws E1, E2;
}
