package com.impressiveinteractive.synapse.exception;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
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
        FormattingTuple tuple = MessageFormatter.arrayFormat(message, args);
        return reduceStackTrace(constructor.apply(tuple.getMessage(), tuple.getThrowable()));
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
        FormattingTuple tuple = MessageFormatter.arrayFormat(message, args);
        Throwable throwable = tuple.getThrowable();
        if (throwable != null) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Unexpected throwable when formatting message.", throwable);
            iae.addSuppressed(reduceStackTrace(constructor.apply(tuple.getMessage())));
            throw iae;
        }
        return reduceStackTrace(constructor.apply(tuple.getMessage()));
    }

    /**
     * Wraps checked exceptions into a {@link WrappedCheckedException}. The type of {@link WrappedCheckedException} is
     * returned depends on the given exception type. Unchecked exceptions pass right through without being wrapped.
     *
     * @param e The exception to make runtime friendly.
     * @return The resulting {@link RuntimeException}.
     */
    public static RuntimeException wrapChecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else if (e instanceof IOException) {
            return new RuntimeIOException((IOException) e);
        } else if (e instanceof InterruptedException) {
            return new RuntimeInterruptedException((InterruptedException) e);
        }
        return new WrappedCheckedException(e);
    }

    /**
     * Wrap the given {@link ExceptionalConsumer} in a regular {@link Consumer} that will
     * {@link #wrapChecked(Exception) wrap checked exceptions} into their runtime equivalent. Can be used to have
     * checked exceptions in lambda apis like so:
     * <pre>
     * void consume(String consumable) throws IOException;
     *
     * try {
     *     Stream.of("Apple", "Orange")
     *             .forEach(Exceptions.wrap(this::consume));
     * } catch (RuntimeIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param consumer The consumer throwing checked exceptions.
     * @param <T>      The type of object consumed by the consumer.
     * @param <E>      The checked exception type thrown by the consumer.
     * @return A {@link Consumer} that uses {@link #wrapChecked(Exception)} and therefore throws no checked exceptions.
     */
    public static <T, E extends Exception> Consumer<T> wrap(ExceptionalConsumer<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw wrapChecked(e);
            }
        };
    }

    /**
     * Wrap the given {@link ExceptionalSupplier} in a regular {@link Supplier} that will
     * {@link #wrapChecked(Exception) wrap checked exceptions} into their runtime equivalent. Can be used to have
     * checked exceptions in lambda apis like so:
     * <pre>
     * String supply() throws IOException;
     *
     * try {
     *     Stream.generate(Exceptions.wrap(this::supply))
     *             .forEach(LOGGER::info);
     * } catch (RuntimeIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param supplier The supplier throwing checked exceptions.
     * @param <T>      The type of object supplier by the supplier.
     * @param <E>      The checked exception type thrown by the supplier.
     * @return A {@link Supplier} that uses {@link #wrapChecked(Exception)} and therefore throws no checked exceptions.
     */
    public static <T, E extends Exception> Supplier<T> wrap(ExceptionalSupplier<T, E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw wrapChecked(e);
            }
        };
    }

    /**
     * Wrap the given {@link ExceptionalFunction} in a regular {@link Function} that will
     * {@link #wrapChecked(Exception) wrap checked exceptions} into their runtime equivalent. Can be used to have
     * checked exceptions in lambda apis like so:
     * <pre>
     * String transform(String consumable) throws IOException;
     *
     * try {
     *     Stream.of("Apple", "Orange")
     *             .map(Exceptions.wrap(this::transform))
     *             .forEach(LOGGER::info);
     * } catch (RuntimeIOException e) {
     *     e.unwrap(); // Throws original IOException
     * }
     * </pre>
     *
     * @param function The function throwing checked exceptions.
     * @param <I>      The input argument type for the function.
     * @param <O>      The return type for the function.
     * @param <E>      The checked exception type thrown by the function.
     * @return A {@link Function} that uses {@link #wrapChecked(Exception)} and therefore throws no checked exceptions.
     */
    public static <I, O, E extends Exception> Function<I, O> wrap(ExceptionalFunction<I, O, E> function) {
        return i -> {
            try {
                return function.apply(i);
            } catch (Exception e) {
                throw wrapChecked(e);
            }
        };
    }

    private static <T extends Throwable> T reduceStackTrace(T exception) {
        StackTraceElement[] originalStackTrace = exception.getStackTrace();
        exception.setStackTrace(Arrays.copyOfRange(originalStackTrace, 1, originalStackTrace.length));
        return exception;
    }
}
