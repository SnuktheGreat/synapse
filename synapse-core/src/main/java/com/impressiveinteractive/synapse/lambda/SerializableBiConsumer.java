package com.impressiveinteractive.synapse.lambda;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface SerializableBiConsumer<T, U> extends BiConsumer<T, U>, SerializableLambda {

    @SuppressWarnings("unchecked")
    default Class<T> getInputClass1() {
        return (Class<T>) Lambdas.getRawParameterType(serialized(), 0);
    }

    @SuppressWarnings("unchecked")
    default Class<U> getInputClass2() {
        return (Class<U>) Lambdas.getRawParameterType(serialized(), 1);
    }
}