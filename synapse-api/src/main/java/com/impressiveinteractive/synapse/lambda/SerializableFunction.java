package com.impressiveinteractive.synapse.lambda;

import java.util.function.Function;

@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, SerializableLambda {

}