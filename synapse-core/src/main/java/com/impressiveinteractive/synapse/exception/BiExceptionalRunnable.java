package com.impressiveinteractive.synapse.exception;

@FunctionalInterface
public interface BiExceptionalRunnable<E1 extends Exception, E2 extends Exception> {
    void run() throws E1, E2;
}
