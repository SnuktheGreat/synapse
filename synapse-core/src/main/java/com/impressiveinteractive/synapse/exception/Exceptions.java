package com.impressiveinteractive.synapse.exception;

import com.impressiveinteractive.synapse.lambda.Lambdas;
import com.impressiveinteractive.synapse.lambda.SerializableFunction;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides utility methods to deal with {@link Throwable} instances. Called {@link Exceptions} (mostly) to avoid
 * collisions with existing libraries.
 */
public final class Exceptions {

    private Exceptions() {
        throw new AssertionError("Illegal private constructor call.");
    }

    // Attempt 1: In this attempt I tried to use generics to define all possibly thrown exceptions and then consume
    // them one by one until none remained. This is compile time safe here, but breaks down when supplying the
    // consumers, which I've only been able to make work when accepting Exception.
    public static <E1 extends Exception, E2 extends Exception, E3 extends Exception> void wrap(
            TriExceptionalRunnable<E1, E2, E3> runnable,
            BiExceptionalFunction<Exception, RuntimeException, E2, E3> consumer1,
            ExceptionalFunction<Exception, RuntimeException, E3> consumer2,
            Function<Exception, RuntimeException> consumer3) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw wrap(
                    (BiExceptionalCallable<RuntimeException, E2, E3>) () -> {
                        throw consumer1.apply(e);
                    },
                    consumer2,
                    consumer3);
        }
    }

    public static <V, E1 extends Exception, E2 extends Exception, E3 extends Exception> V wrap(
            TriExceptionalCallable<V, E1, E2, E3> callable,
            BiExceptionalFunction<Exception, RuntimeException, E2, E3> consumer1,
            ExceptionalFunction<Exception, RuntimeException, E3> consumer2,
            Function<Exception, RuntimeException> consumer3) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw wrap(
                    (BiExceptionalCallable<RuntimeException, E2, E3>) () -> {
                        throw consumer1.apply(e);
                    },
                    consumer2,
                    consumer3);
        }
    }

    public static <E1 extends Exception, E2 extends Exception> void wrap(
            BiExceptionalRunnable<E1, E2> runnable,
            ExceptionalFunction<Exception, RuntimeException, E2> consumer1,
            Function<Exception, RuntimeException> consumer2) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw wrap(
                    (ExceptionalCallable<RuntimeException, E2>) () -> {
                        throw consumer1.apply(e);
                    },
                    consumer2);
        }
    }

    public static <V, E1 extends Exception, E2 extends Exception> V wrap(
            BiExceptionalCallable<V, E1, E2> callable,
            ExceptionalFunction<Exception, RuntimeException, E2> consumer1,
            Function<Exception, RuntimeException> consumer2) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw wrap(
                    (ExceptionalCallable<RuntimeException, E2>) () -> {
                        throw consumer1.apply(e);
                    },
                    consumer2);
        }
    }

    private static <E1 extends Exception> void wrap(
            ExceptionalRunnable<E1> runnable,
            Function<Exception, RuntimeException> consumer) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw consumer.apply(e);
        }
    }

    private static <V, E1 extends Exception> V wrap(
            ExceptionalCallable<V, E1> callable,
            Function<Exception, RuntimeException> consumer) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw consumer.apply(e);
        }
    }

    // Attempt 2: Significantly simpler than attempt 1, but requires the use of lambda serialization and reflection,
    // which isn't great. The deal breaker however is the fact that the BiExceptionalCallable can not determine the
    // exception types from a method reference, defaulting to Exception for both E1 and E2, even if the signature
    // clearly defines two types.
    public static <V, E1 extends Exception, E2 extends Exception> V wrap(
            BiExceptionalCallable<V, E1, E2> callable,
            SerializableFunction<E1, ? extends RuntimeException> wrapper1,
            SerializableFunction<E2, ? extends RuntimeException> wrapper2
    ) {
        try {
            return callable.call();
        } catch (Exception e) {
            if (Lambdas.getRawParameterType(wrapper1.serialized(), 0).isAssignableFrom(e.getClass())) {
                throw wrapper1.apply((E1) e);
            } else if (Lambdas.getRawParameterType(wrapper2.serialized(), 0).isAssignableFrom(e.getClass())) {
                throw wrapper2.apply((E2) e);
            }
            throw format(IllegalStateException::new, "Wrappers did not wrap type {}.", e.getClass(), e);
        }
    }

    public static <V, E1 extends Exception, E2 extends Exception, E3 extends Exception> V wrap(
            TriExceptionalCallable<V, E1, E2, E3> callable,
            SerializableFunction<E1, ? extends RuntimeException> wrapper1,
            SerializableFunction<E2, ? extends RuntimeException> wrapper2,
            SerializableFunction<E3, ? extends RuntimeException> wrapper3
    ) {
        try {
            return callable.call();
        } catch (Exception e) {
            if (Lambdas.getRawParameterType(wrapper1.serialized(), 0).isAssignableFrom(e.getClass())) {
                throw wrapper1.apply((E1) e);
            } else if (Lambdas.getRawParameterType(wrapper2.serialized(), 0).isAssignableFrom(e.getClass())) {
                throw wrapper2.apply((E2) e);
            } else if (Lambdas.getRawParameterType(wrapper3.serialized(), 0).isAssignableFrom(e.getClass())) {
                throw wrapper3.apply((E3) e);
            }
            throw format(IllegalStateException::new, "Wrappers did not wrap type {}.", e.getClass(), e);
        }
    }

    /**
     * Use SLF4J style formatting on a given {@link Throwable Throwable's} message and cause constructor.
     * <p>
     * Example:
     * <pre>
     * IOException formatted = Exceptions.format(IOException::new,
     *         "Testing {}, {}, {}.", "one", "two", "three", cause);
     *
     * assertThat(formatted.getMessage(), is(equalTo("Testing one, two, three.")));
     * assertThat(formatted.getCause(), is(cause));
     * </pre>
     *
     * @param constructor A lambda that should be a method reference to a {@link Throwable} constructor like
     *                    {@link Throwable#Throwable(String, Throwable)}.
     * @param message     The message of the exception.
     * @param args        Arguments that will replace {} tokens in the given message. Optionally the final object can
     *                    be a {@link Throwable} that will be used as the result's {@link Throwable#getCause() cause}.
     * @param <T>         The type of {@link Throwable} created from this message.
     * @return The {@link Throwable} with the formatted message and an optional cause.
     */
    public static <T extends Throwable> T format(
            BiFunction<String, Throwable, T> constructor, String message, Object... args) {
        ExceptionalMessage exceptionalMessage = ExceptionalMessage.parse(message, args);
        return reduceStackTrace(constructor.apply(exceptionalMessage.getMessage(), exceptionalMessage.getThrowable()));
    }

    /**
     * Use SLF4J style formatting on a given {@link Throwable Throwable's} message constructor. This method does not
     * allow the presence of a final {@link Throwable} cause like {@link #format(BiFunction, String, Object...)} and
     * should only be used of no causal constructor is available for the given {@link Throwable} type.
     * <p>
     * Example:
     * <pre>
     * IOException formatted = Exceptions.formatMessage(IOException::new,
     *         "Testing {}, {}, {}.", "one", "two", "three");
     *
     * assertThat(formatted.getMessage(), is(equalTo("Testing one, two, three.")));
     * assertThat(formatted.getCause(), is(nullValue()));
     * </pre>
     *
     * @param constructor A lambda that should be a method reference to a {@link Throwable} constructor like
     *                    {@link Throwable#Throwable(String)}.
     * @param message     The message of the exception.
     * @param args        Arguments that will replace {} tokens in the given message. The final object <strong>must not
     *                    </strong> be a {@link Throwable} cause.
     * @param <T>         The type of {@link Throwable} created from this message.
     * @return The {@link Throwable} with the formatted message.
     * @see #format(BiFunction, String, Object...)
     */
    public static <T extends Throwable> T formatMessage(
            Function<String, T> constructor, String message, Object... args) {
        ExceptionalMessage exceptionalMessage = ExceptionalMessage.parse(message, args);
        Throwable throwable = exceptionalMessage.getThrowable();
        if (throwable != null) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Unexpected throwable when formatting message.", throwable);
            iae.addSuppressed(reduceStackTrace(constructor.apply(exceptionalMessage.getMessage())));
            throw iae;
        }
        return reduceStackTrace(constructor.apply(exceptionalMessage.getMessage()));
    }

    /**
     * Wrap the given {@link ExceptionalConsumer} in a regular {@link Consumer}. When the exceptional consumer throws
     * the checked exception type, it will be wrapped and thrown as the {@link RuntimeException} produced by the given
     * wrapper. Example:
     * <pre>
     * // The culprit
     * void consume(String consumable) throws IOException;
     *
     * ...
     *
     * Stream.of("Apple", "Orange")
     *         .forEach(Exceptions.wrap(this::consume, RuntimeIOException::new));
     * </pre>
     * <p>
     * If you want to rethrow the original checked exception, you can do this:
     * <pre>
     * try {
     *     Stream.of("Apple", "Orange")
     *             .forEach(Exceptions.wrap(this::consume, WrappedIOException::new));
     * } catch (WrappedIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param consumer The consumer throwing checked exceptions.
     * @param wrapper  Transforms the checked exception to an unchecked exception.
     * @param <T>      The type of object consumed by the consumer.
     * @param <E>      The checked exception type thrown by the {@link ExceptionalConsumer} and wrapped by the wrapper.
     * @return A regular {@link Consumer}.
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Exception> Consumer<T> wrapExceptionalConsumer(
            ExceptionalConsumer<T, E> consumer,
            Function<E, ? extends RuntimeException> wrapper) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw wrapper.apply((E) e);
            }
        };
    }

    /**
     * Wrap the given {@link ExceptionalSupplier} in a regular {@link Supplier}. When the exceptional supplier throws
     * the checked exception type, it will be wrapped and thrown as the {@link RuntimeException} produced by the given
     * wrapper. Example:
     * <pre>
     * // The culprit
     * String supply() throws IOException;
     *
     * ...
     *
     * Stream.generate(Exceptions.wrap(this::supply, RuntimeIOException::new))
     *         .forEach(LOGGER::info);
     * </pre>
     * <p>
     * If you want to rethrow the original checked exception, you can do this:
     * <pre>
     * try {
     *     Stream.generate(Exceptions.wrap(this::supply, WrappedIOException::new))
     *             .forEach(LOGGER::info);
     * } catch (WrappedIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param supplier The supplier throwing checked exceptions.
     * @param wrapper  Transforms the checked exception to an unchecked exception.
     * @param <T>      The type of object supplier by the supplier.
     * @param <E>      The checked exception type thrown by the {@link ExceptionalSupplier} and wrapped by the wrapper.
     * @return A regular {@link Supplier}.
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Exception> Supplier<T> wrapExceptionalSupplier(
            ExceptionalSupplier<T, E> supplier,
            Function<E, ? extends RuntimeException> wrapper) {
        return () -> {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw wrapper.apply((E) e);
            }
        };
    }

    /**
     * Wrap the given {@link ExceptionalFunction} in a regular {@link Function}. When the exceptional function throws
     * the checked exception type, it will be wrapped and thrown as the {@link RuntimeException} produced by the given
     * wrapper. Example:
     * <pre>
     * // The culprit
     * String transform(String consumable) throws IOException;
     *
     * ...
     *
     *
     * Stream.of("Apple", "Orange")
     *         .map(Exceptions.wrap(this::transform, RuntimeIOException::new))
     *         .forEach(LOGGER::info);
     * </pre>
     * <p>
     * If you want to rethrow the original checked exception, you can do this:
     * <pre>
     * try {
     *     Stream.of("Apple", "Orange")
     *             .map(Exceptions.wrap(this::transform, WrappedIOException::new))
     *             .forEach(LOGGER::info);
     * } catch (WrappedIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param function The function throwing checked exceptions.
     * @param wrapper  Transforms the checked exception to an unchecked exception.
     * @param <I>      The input argument type for the function.
     * @param <O>      The return type for the function.
     * @param <E>      The checked exception type thrown by the {@link ExceptionalFunction}.
     * @return A regular {@link Function}.
     */
    @SuppressWarnings("unchecked")
    public static <I, O, E extends Exception> Function<I, O> wrapExceptionalFunction(
            ExceptionalFunction<I, O, E> function,
            Function<E, ? extends RuntimeException> wrapper) {
        return i -> {
            try {
                return function.apply(i);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw wrapper.apply((E) e);
            }
        };
    }

    /**
     * Short for {@link #wrapExceptionalConsumer(ExceptionalConsumer, Function)}.
     * <p>
     * Wrap the given {@link ExceptionalConsumer} in a regular {@link Consumer}. When the exceptional consumer throws
     * the checked exception type, it will be wrapped and thrown as the {@link RuntimeException} produced by the given
     * wrapper. Example:
     * <pre>
     * // The culprit
     * void consume(String consumable) throws IOException;
     *
     * ...
     *
     * Stream.of("Apple", "Orange")
     *         .forEach(Exceptions.wrap(this::consume, RuntimeIOException::new));
     * </pre>
     * <p>
     * If you want to rethrow the original checked exception, you can do this:
     * <pre>
     * try {
     *     Stream.of("Apple", "Orange")
     *             .forEach(Exceptions.wrap(this::consume, WrappedIOException::new));
     * } catch (WrappedIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param consumer The consumer throwing checked exceptions.
     * @param wrapper  Transforms the checked exception to an unchecked exception.
     * @param <T>      The type of object consumed by the consumer.
     * @param <E>      The checked exception type thrown by the {@link ExceptionalConsumer} and wrapped by the wrapper.
     * @return A regular {@link Consumer}.
     */
    public static <T, E extends Exception> Consumer<T> wrapExceptional(
            ExceptionalConsumer<T, E> consumer,
            Function<E, ? extends RuntimeException> wrapper) {
        return wrapExceptionalConsumer(consumer, wrapper);
    }

    /**
     * Short for {@link #wrapExceptionalSupplier(ExceptionalSupplier, Function)}
     * <p>
     * Wrap the given {@link ExceptionalSupplier} in a regular {@link Supplier}. When the exceptional supplier throws
     * the checked exception type, it will be wrapped and thrown as the {@link RuntimeException} produced by the given
     * wrapper. Example:
     * <pre>
     * // The culprit
     * String supply() throws IOException;
     *
     * ...
     *
     * Stream.generate(Exceptions.wrap(this::supply, RuntimeIOException::new))
     *         .forEach(LOGGER::info);
     * </pre>
     * <p>
     * If you want to rethrow the original checked exception, you can do this:
     * <pre>
     * try {
     *     Stream.generate(Exceptions.wrap(this::supply, WrappedIOException::new))
     *             .forEach(LOGGER::info);
     * } catch (WrappedIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param supplier The supplier throwing checked exceptions.
     * @param wrapper  Transforms the checked exception to an unchecked exception.
     * @param <T>      The type of object supplier by the supplier.
     * @param <E>      The checked exception type thrown by the {@link ExceptionalSupplier} and wrapped by the wrapper.
     * @return A regular {@link Supplier}.
     */
    public static <T, E extends Exception> Supplier<T> wrapExceptional(
            ExceptionalSupplier<T, E> supplier,
            Function<E, ? extends RuntimeException> wrapper) {
        return wrapExceptionalSupplier(supplier, wrapper);
    }

    /**
     * Short for {@link #wrapExceptionalFunction(ExceptionalFunction, Function)}
     * <p>
     * Wrap the given {@link ExceptionalFunction} in a regular {@link Function}. When the exceptional function throws
     * the checked exception type, it will be wrapped and thrown as the {@link RuntimeException} produced by the given
     * wrapper. Example:
     * <pre>
     * // The culprit
     * String transform(String consumable) throws IOException;
     *
     * ...
     *
     *
     * Stream.of("Apple", "Orange")
     *         .map(Exceptions.wrap(this::transform, RuntimeIOException::new))
     *         .forEach(LOGGER::info);
     * </pre>
     * <p>
     * If you want to rethrow the original checked exception, you can do this:
     * <pre>
     * try {
     *     Stream.of("Apple", "Orange")
     *             .map(Exceptions.wrap(this::transform, WrappedIOException::new))
     *             .forEach(LOGGER::info);
     * } catch (WrappedIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param function The function throwing checked exceptions.
     * @param wrapper  Transforms the checked exception to an unchecked exception.
     * @param <I>      The input argument type for the function.
     * @param <O>      The return type for the function.
     * @param <E>      The checked exception type thrown by the {@link ExceptionalFunction}.
     * @return A regular {@link Function}.
     */
    public static <I, O, E extends Exception> Function<I, O> wrapExceptional(
            ExceptionalFunction<I, O, E> function,
            Function<E, ? extends RuntimeException> wrapper) {
        return wrapExceptionalFunction(function, wrapper);
    }

    private static <T extends Throwable> T reduceStackTrace(T exception) {
        StackTraceElement[] originalStackTrace = exception.getStackTrace();
        exception.setStackTrace(Arrays.copyOfRange(originalStackTrace, 1, originalStackTrace.length));
        return exception;
    }
}
