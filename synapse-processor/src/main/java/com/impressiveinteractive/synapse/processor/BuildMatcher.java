package com.impressiveinteractive.synapse.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation is processed to create a Hamcrest matcher for pojos. The generated matcher will contain a DSL that
 * allows developers to test the results of all getter-like methods on the pojo. It is also possible to add further
 * methods using utility classes, where all public static methods that convert the pojo type to another object type are
 * added to the matcher.
 * <p>
 * It is recommended to put these on the test classes where the matcher will be used.
 * <p>
 * Examples of generated matchers and their uses can be found in the source code of the synapse-processor-test module.
 */
@Target(ElementType.TYPE)
public @interface BuildMatcher {
    /**
     * Alias for {@link #pojo()}.
     *
     * @return The class to generate the Hamcrest matcher for.
     */
    Class<?> value() default Object.class; // Alias of pojo

    /**
     * @return The class to generate the Hamcrest matcher for.
     */
    Class<?> pojo() default Object.class;

    /**
     * @return The package where the matcher will be generated into. Will use the {@link #pojo()} package on empty.
     */
    String destinationPackage() default "";

    /**
     * @return The name of the generated matcher. Will use the {@link #pojo()} name + "Matcher" on empty.
     */
    String destinationName() default "";

    /**
     * @return The name of the static method that creates an instance of the matcher. Will use the {@link #pojo()} name,
     * starting with a lowercase letter on empty.
     */
    String staticMethodName() default "";

    /**
     * @return Utility classes containing further public static methods that convert the {@link #pojo()} type to
     * something else.
     */
    Class<?>[] utilities() default {};

    /**
     * @return Whether to skip methods in {@link Object}. Overridden methods are always added.
     */
    boolean skipObjectMethods() default false;
}
