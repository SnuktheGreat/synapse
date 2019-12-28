package com.impressiveinteractive.synapse.exception;

@FunctionalInterface
public interface ExceptionalPredicate<T, E extends Exception> {

    boolean test(T t) throws E;
}
