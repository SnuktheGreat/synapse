package com.impressiveinteractive.synapse.exception;

/**
 * Like a regular {@link java.util.function.Function Function}, but can throw checked exceptions. It was designed to
 * work with {@link Exceptions#wrap(ExceptionalFunction)} to convert checked exceptions into their runtime equivalent.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of exception that can be thrown
 * @see Exceptions#wrap(ExceptionalFunction)
 */
@FunctionalInterface
public interface ExceptionalFunction<T, R, E extends Exception> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws E thrown in exceptional circumstances
     */
    R apply(T t) throws E;
}