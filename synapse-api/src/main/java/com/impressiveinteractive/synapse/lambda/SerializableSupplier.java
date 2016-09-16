package com.impressiveinteractive.synapse.lambda;

import java.util.function.Supplier;

@FunctionalInterface
public interface SerializableSupplier<T> extends Supplier<T>, SerializableLambda {

}
