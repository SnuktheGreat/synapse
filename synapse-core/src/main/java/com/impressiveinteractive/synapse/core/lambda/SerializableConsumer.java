package com.impressiveinteractive.synapse.core.lambda;

import java.util.function.Consumer;

/**
 * The serializable equivalent of {@link Consumer}.
 *
 * @param <T> The type of the input to the operation.
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, SerializableLambda {

    /**
     * @return The raw type of the input to the operation.
     */
    @SuppressWarnings("unchecked")
    default Class<T> getInputClass() {
        return (Class<T>) Lambdas.getRawParameterType(serialized(), 0);
    }
}