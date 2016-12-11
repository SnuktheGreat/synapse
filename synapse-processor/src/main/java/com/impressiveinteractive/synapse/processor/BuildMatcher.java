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
     * @return Utility classes containing further public static methods that convert the {@link #pojo()} type to
     * something else.
     */
    Class<?>[] utilities() default {};
}
