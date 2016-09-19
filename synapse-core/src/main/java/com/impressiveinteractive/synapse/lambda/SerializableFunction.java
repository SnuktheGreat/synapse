package com.impressiveinteractive.synapse.lambda;

import java.util.function.Function;

/**
 * The serializable equivalent of {@link Function}.
 *
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 * @see SerializableLambda
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, SerializableLambda {

    /**
     * @return The raw type of the input to the function.
     */
    @SuppressWarnings("unchecked")
    default Class<T> getInputClass() {
        return (Class<T>) Lambdas.getRawParameterType(serialized(), 0);
    }

    /**
     * @return The raw type of the result of the function.
     */
    @SuppressWarnings("unchecked")
    default Class<R> getResultClass() {
        return (Class<R>) Lambdas.getRawReturnType(serialized());
    }
}