package com.impressiveinteractive.synapse.exception;

/**
 * Like a regular {@link java.util.function.Consumer Consumer}, but can throw checked exceptions. It was designed to
 * work with {@link Exceptions#wrap(ExceptionalConsumer)} to convert checked exceptions into their runtime equivalent.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of exception that can be thrown
 * @see Exceptions#wrap(ExceptionalConsumer)
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