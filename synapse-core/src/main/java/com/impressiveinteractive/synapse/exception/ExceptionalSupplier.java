package com.impressiveinteractive.synapse.exception;

import java.util.function.Function;

/**
 * Like a regular {@link java.util.function.Supplier Supplier}, but can throw checked exceptions. It was designed to
 * work with {@link Exceptions#wrapExceptionalSupplier(ExceptionalSupplier, Function)} to convert checked exceptions
 * into their wrapped equivalent.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of exception that can be thrown
 * @see Exceptions#wrapExceptionalSupplier(ExceptionalSupplier, Function)
 * @see Exceptions#wrapExceptional(ExceptionalSupplier, Function)
 */
@FunctionalInterface
public interface ExceptionalSupplier<T, E extends Exception> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws E thrown in exceptional circumstances
     */
    T get() throws E;
}
