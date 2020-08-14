package com.impressiveinteractive.synapse.exception;

@FunctionalInterface
public interface ExceptionalRunnable<E extends Exception> {
    void run() throws E;
}
