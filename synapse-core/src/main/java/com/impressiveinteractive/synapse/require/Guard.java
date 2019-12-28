package com.impressiveinteractive.synapse.require;

import com.impressiveinteractive.synapse.exception.Exceptions;

import java.util.function.Function;
import java.util.function.Predicate;

public final class Guard {
    private Guard() {
        throw new AssertionError("Illegal private constructor call.");
    }

    public static <T extends Throwable> void requireOrThrow(
            boolean statement, Function<String, T> constructor, String message, Object... args) throws T {
        if (!statement) {
            throw Exceptions.formatMessage(constructor, message, args);
        }
    }

    public static void requireArgument(boolean statement, String message, Object... args) {
        requireOrThrow(statement, IllegalArgumentException::new, message, args);
    }

    public static void requireState(boolean statement, String message, Object... args) {
        requireOrThrow(statement, IllegalStateException::new, message, args);
    }

    public static <E, T extends Throwable> E requireOrThrow(
            E item, Predicate<E> predicate, Function<String, T> constructor, String message, Object... args) throws T {
        if (!predicate.test(item)) {
            throw Exceptions.formatMessage(constructor, message, args);
        }
        return item;
    }

    public static <E> E requireArgument(E item, Predicate<E> predicate, String message, Object... args) {
        return requireOrThrow(item, predicate, IllegalArgumentException::new, message, args);
    }

    public static <E> E requireState(E item, Predicate<E> predicate, String message, Object... args) {
        return requireOrThrow(item, predicate, IllegalStateException::new, message, args);
    }
}
