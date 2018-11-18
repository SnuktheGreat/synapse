package com.impressiveinteractive.synapse.core.lambda;

import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.util.List;

/**
 * Utility class for lambdas. This class is mostly built around the {@link SerializableLambda}, which can be used to
 * extract additional information about where the lambda is created, what kind of lambda it is, etc.
 */
public final class Lambdas {

    private Lambdas() {
        throw new AssertionError("Calling private constructor. No instances should be created from this class.");
    }

    /**
     * Create a {@link SerializableConsumer}. The method itself does nothing, but because the parameter is of the
     * {@link SerializableConsumer} type, it will return any lambda as such.
     *
     * @param consumer The {@link SerializableConsumer}.
     * @param <T>      The type of the input to the operation.
     * @return The {@link SerializableConsumer}.
     * @see #serializable(SerializableConsumer)
     */
    public static <T> SerializableConsumer<T> serializableConsumer(SerializableConsumer<T> consumer) {
        return consumer;
    }

    /**
     * Create a {@link SerializableFunction}. The method itself does nothing, but because the parameter is of the
     * {@link SerializableFunction} type, it will return any lambda as such.
     *
     * @param function The {@link SerializableFunction}.
     * @param <T>      The type of the input to the function.
     * @param <R>      The type of the result of the function.
     * @return The {@link SerializableFunction}.
     */
    public static <T, R> SerializableFunction<T, R> serializableFunction(SerializableFunction<T, R> function) {
        return function;
    }

    /**
     * Create a {@link SerializableSupplier}. The method itself does nothing, but because the parameter is of the
     * {@link SerializableSupplier} type, it will return any lambda as such.
     *
     * @param supplier The {@link SerializableSupplier}.
     * @param <T>      The type of results supplied by the supplier.
     * @return The {@link SerializableSupplier}.
     */
    public static <T> SerializableSupplier<T> serializableSupplier(SerializableSupplier<T> supplier) {
        return supplier;
    }

    /**
     * Short for {@link #serializableConsumer(SerializableConsumer)}. Use the long version if the type of lambda given
     * causes signature clashes with any of the other {@code serializable(...)} methods.
     *
     * @param consumer The {@link SerializableConsumer}.
     * @param <T>      The type of the input to the operation.
     * @return The {@link SerializableConsumer}.
     */
    public static <T> SerializableConsumer<T> serializable(SerializableConsumer<T> consumer) {
        return serializableConsumer(consumer);
    }

    /**
     * Short for {@link #serializableFunction(SerializableFunction)}. Use the long version if the type of lambda given
     * causes signature clashes with any of the other {@code serializable(...)} methods.
     *
     * @param function The {@link SerializableFunction}.
     * @param <T>      The type of the input to the function.
     * @param <R>      The type of the result of the function.
     * @return The {@link SerializableFunction}.
     */
    public static <T, R> SerializableFunction<T, R> serializable(SerializableFunction<T, R> function) {
        return serializableFunction(function);
    }

    /**
     * Short for {@link #serializableSupplier(SerializableSupplier)}. Use the long version if the type of lambda given
     * causes signature clashes with any of the other {@code serializable(...)} methods.
     *
     * @param supplier The {@link SerializableSupplier}.
     * @param <T>      The type of results supplied by the supplier.
     * @return The {@link SerializableSupplier}.
     */
    public static <T> SerializableSupplier<T> serializable(SerializableSupplier<T> supplier) {
        return serializableSupplier(supplier);
    }

    /**
     * Get the raw return type for the given {@link SerializedLambda}.
     *
     * @param lambda The {@link SerializedLambda}.
     * @return The raw return type for the given {@link SerializedLambda}.
     * @see SerializableLambda#serialized()
     */
    public static Class<?> getRawReturnType(SerializedLambda lambda) {
        MethodType methodType = MethodType.fromMethodDescriptorString(lambda.getImplMethodSignature(),
                Lambdas.class.getClassLoader());
        return methodType.returnType();
    }

    /**
     * Get the raw type for all parameters on the given {@link SerializedLambda}.
     *
     * @param lambda The {@link SerializedLambda}.
     * @return The raw type for all parameters on the given {@link SerializedLambda}.
     * @see SerializableLambda#serialized()
     */
    public static List<Class<?>> getRawParameterTypes(SerializedLambda lambda) {
        MethodType methodType = MethodType.fromMethodDescriptorString(lambda.getImplMethodSignature(),
                Lambdas.class.getClassLoader());
        return methodType.parameterList();
    }

    /**
     * Get the raw parameter type for the parameter at index {@code i} on the given {@link SerializedLambda}.
     *
     * @param lambda The {@link SerializedLambda}.
     * @param i      The index of the parameter.
     * @return The raw parameter type for the parameter at index {@code i} on the given {@link SerializedLambda}.
     * @see SerializableLambda#serialized()
     */
    public static Class<?> getRawParameterType(SerializedLambda lambda, int i) {
        MethodType methodType = MethodType.fromMethodDescriptorString(lambda.getImplMethodSignature(),
                Lambdas.class.getClassLoader());
        return methodType.parameterType(i);
    }
}