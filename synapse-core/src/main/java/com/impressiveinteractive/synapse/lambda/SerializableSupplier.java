package com.impressiveinteractive.synapse.lambda;

import java.util.function.Supplier;

/**
 * The serializable equivalent of {@link Supplier}.
 *
 * @param <T> The type of results supplied by the supplier
 * @see SerializableLambda
 */
@FunctionalInterface
public interface SerializableSupplier<T> extends Supplier<T>, SerializableLambda {

    /**
     * @return The raw type of results supplied by the supplier
     */
    @SuppressWarnings("unchecked")
    default Class<T> getResultClass() {
        return (Class<T>) Lambdas.getRawReturnType(serialized());
    }
}
