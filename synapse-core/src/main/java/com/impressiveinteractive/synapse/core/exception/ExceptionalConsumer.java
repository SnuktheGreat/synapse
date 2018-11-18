package com.impressiveinteractive.synapse.core.exception;

import java.util.function.Function;

/**
 * Like a regular {@link java.util.function.Consumer Consumer}, but can throw checked exceptions. It was designed to
 * work with {@link Exceptions#wrapExceptionalConsumer(ExceptionalConsumer, Function)} to convert checked exceptions
 * into their wrapped equivalent.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of exception that can be thrown
 * @see Exceptions#wrapExceptionalConsumer(ExceptionalConsumer, Function)
 * @see Exceptions#wrapExceptional(ExceptionalConsumer, Function)
 */
@FunctionalInterface
public interface ExceptionalConsumer<T, E extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws E thrown in exceptional circumstances
     */
    void accept(T t) throws E;
}