package com.impressiveinteractive.synapse.core.lambda;

import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LambdasTest {
    @Test
    public void testConsumer() {
        assertThat(Lambdas.serializable(this::consumer).getInputClass(), is(equalTo(String.class)));
    }

    @Test
    public void testFunction() {
        assertThat(Lambdas.serializable(this::transform).getInputClass(), is(equalTo(int.class)));
        assertThat(Lambdas.serializable(this::transform).getResultClass(), is(equalTo(String.class)));
    }

    @Test
    public void testSupplier() {
        assertThat(Lambdas.serializable(this::supply).getResultClass(), is(equalTo(String.class)));
    }

    @Test
    public void getRawParameterTypes() {
        SerializableBiConsumer<String, Integer> biConsumer = this::consume;
        List<Class<?>> types = Lambdas.getRawParameterTypes(biConsumer.serialized());

        assertThat(types.get(0), is(equalTo(String.class)));
        assertThat(types.get(1), is(equalTo(int.class)));
    }

    public void consumer(String consumable) {
        // noop
    }

    public String transform(int in) {
        return null; // noop
    }

    public String supply() {
        return null; // noop
    }

    public void consume(String a, int b) {
        // noop
    }

    private interface SerializableBiConsumer<T, U> extends BiConsumer<T, U>, SerializableLambda {

    }
}