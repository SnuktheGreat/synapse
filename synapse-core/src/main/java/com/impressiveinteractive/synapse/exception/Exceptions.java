package com.impressiveinteractive.synapse.exception;

import com.impressiveinteractive.synapse.exception.runtime.RuntimeClassNotFoundException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeExecutionException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeIOException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeIllegalAccessException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeInstantiationException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeInterruptedException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeInvocationTargetException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeNoSuchFieldException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeNoSuchMethodException;
import com.impressiveinteractive.synapse.exception.runtime.RuntimeReflectiveOperationException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides utility methods to deal with {@link Throwable} instances. Called {@link Exceptions} (mostly) to avoid
 * collisions with existing libraries.
 */
public final class Exceptions {

    private Exceptions() {
        throw new AssertionError("Illegal private constructor call.");
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

    public static RuntimeClassNotFoundException uncheck(ClassNotFoundException e) {
        return new RuntimeClassNotFoundException(e);
    }

    public static RuntimeClassNotFoundException uncheck(ClassNotFoundException e, String message, Object... args) {
        return Exceptions.format(RuntimeClassNotFoundException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeExecutionException uncheck(ExecutionException e) {
        return new RuntimeExecutionException(e);
    }

    public static RuntimeExecutionException uncheck(ExecutionException e, String message, Object... args) {
        return Exceptions.format(RuntimeExecutionException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeIllegalAccessException uncheck(IllegalAccessException e) {
        return new RuntimeIllegalAccessException(e);
    }

    public static RuntimeIllegalAccessException uncheck(IllegalAccessException e, String message, Object... args) {
        return Exceptions.format(RuntimeIllegalAccessException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeInstantiationException uncheck(InstantiationException e) {
        return new RuntimeInstantiationException(e);
    }

    public static RuntimeInstantiationException uncheck(InstantiationException e, String message, Object... args) {
        return Exceptions.format(RuntimeInstantiationException::new,
                message, Stream.concat(Arrays.stream(args),
                        Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeInterruptedException uncheck(InterruptedException e) {
        return RuntimeInterruptedException.interruptAndCreate(e);
    }

    public static RuntimeInterruptedException uncheck(InterruptedException e, String message, Object... args) {
        return Exceptions.format(RuntimeInterruptedException::interruptAndCreate,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeInvocationTargetException uncheck(InvocationTargetException e) {
        return new RuntimeInvocationTargetException(e);
    }

    public static RuntimeInvocationTargetException uncheck(InvocationTargetException e, String message, Object... args) {
        return Exceptions.format(RuntimeInvocationTargetException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeIOException uncheck(IOException e) {
        return new RuntimeIOException(e);
    }

    public static RuntimeIOException uncheck(IOException e, String message, Object... args) {
        return Exceptions.format(RuntimeIOException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeNoSuchFieldException uncheck(NoSuchFieldException e) {
        return new RuntimeNoSuchFieldException(e);
    }

    public static RuntimeNoSuchFieldException uncheck(NoSuchFieldException e, String message, Object... args) {
        return Exceptions.format(RuntimeNoSuchFieldException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeNoSuchMethodException uncheck(NoSuchMethodException e) {
        return new RuntimeNoSuchMethodException(e);
    }

    public static RuntimeNoSuchMethodException uncheck(NoSuchMethodException e, String message, Object... args) {
        return Exceptions.format(RuntimeNoSuchMethodException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    public static RuntimeReflectiveOperationException uncheck(ReflectiveOperationException e) {
        return new RuntimeReflectiveOperationException(e);
    }

    public static RuntimeReflectiveOperationException uncheck(ReflectiveOperationException e, String message, Object... args) {
        return Exceptions.format(RuntimeReflectiveOperationException::new,
                message, Stream.concat(Arrays.stream(args), Stream.of(e)).toArray(Object[]::new));
    }

    @SuppressWarnings("unchecked")
    public static <E extends Exception> Runnable wrapExceptionalRunnable(
            ExceptionalRunnable<E> predicate,
            Function<E, ? extends RuntimeException> wrapper) {
        return () -> {
            try {
                predicate.run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw wrapper.apply((E) e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Exception> Predicate<T> wrapExceptionalPredicate(
            ExceptionalPredicate<T, E> predicate,
            Function<E, ? extends RuntimeException> wrapper) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw wrapper.apply((E) e);
            }
        };
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

    public static <T, E extends Exception> T wrap(
            ExceptionalSupplier<T, E> supplier,
            Function<E, ? extends RuntimeException> wrapper){
        return wrapExceptional(supplier, wrapper).get();
    }

    public static <E extends Exception> void wrap(
            ExceptionalRunnable<E> runnable,
            Function<E, ? extends RuntimeException> wrapper){
        wrapExceptionalRunnable(runnable, wrapper).run();
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
