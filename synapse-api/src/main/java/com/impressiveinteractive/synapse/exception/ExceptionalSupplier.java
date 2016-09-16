package com.impressiveinteractive.synapse.exception;

/**
 * Like a regular {@link java.util.function.Supplier Supplier}, but can throw checked exceptions. It was designed to
 * work with {@link Exceptions#wrap(ExceptionalSupplier)} to convert checked exceptions into their runtime equivalent.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of exception that can be thrown
 * @see Exceptions#wrap(ExceptionalSupplier)
 */
@FunctionalInterface
public interface ExceptionalSupplier<T, E extends Exception> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws E;
}
