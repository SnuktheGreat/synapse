package com.impressiveinteractive.synapse;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Dsl<T> {

    private final T wrapped;

    protected Dsl(T wrapped) {
        this.wrapped = wrapped;
    }

    public static <T> Dsl<T> wrap(T wrapped) {
        return new Dsl<>(wrapped);
    }

    public <V> Dsl<T> modify(Consumer<T> modifier) {
        modifier.accept(wrapped);
        return this;
    }

    public <V> Dsl<T> modify(BiConsumer<T, V> setter, V value) {
        setter.accept(wrapped, value);
        return this;
    }

    public T unwrap() {
        return wrapped;
    }
}
