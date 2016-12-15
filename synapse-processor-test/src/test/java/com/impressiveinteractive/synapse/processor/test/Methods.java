package com.impressiveinteractive.synapse.processor.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Methods {
    private Methods() {
        throw new AssertionError("Illegal private constructor call.");
    }

    public static List<String> getWithMethodNames(Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .map(Method::getName)
                .filter(name -> name.startsWith("with"))
                .collect(Collectors.toList());
    }
}
