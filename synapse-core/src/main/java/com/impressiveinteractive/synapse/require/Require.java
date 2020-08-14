package com.impressiveinteractive.synapse.require;

import com.impressiveinteractive.synapse.exception.Exceptions;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Require {
    private Require() {
        throw new AssertionError("Illegal private constructor call.");
    }

    public static <E, T extends Throwable> E orThrow(
            E item, Predicate<E> predicate, Function<String, T> throwableConstructor,
            String message, Object... additionalArgs) throws T {
        if (!predicate.test(item)) {
            Object[] args = Stream.concat(Stream.of(item), Arrays.stream(additionalArgs)).toArray(Object[]::new);
            throw Exceptions.formatMessage(throwableConstructor, message, args);
        }
        return item;
    }

    public static <E> E argument(E item, Predicate<E> predicate, String message, Object... additionalArgs) {
        return orThrow(item, predicate, IllegalArgumentException::new, message, additionalArgs);
    }

    public static <E> E state(E item, Predicate<E> predicate, String message, Object... additionalArgs) {
        return orThrow(item, predicate, IllegalStateException::new, message, additionalArgs);
    }
}
