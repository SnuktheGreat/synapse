package com.impressiveinteractive.synapse.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Container of {@link BuildMatcher} annotations, if more than one Hamcrest matcher should be generated.
 */
@Target(ElementType.TYPE)
public @interface BuildMatchers {
    /**
     * Alias for {@link #matchers()}.
     *
     * @return An array of {@link BuildMatcher} configurations.
     */
    BuildMatcher[] value() default {};

    /**
     * @return An array of {@link BuildMatcher} configurations.
     */
    BuildMatcher[] matchers() default {};

    /**
     * @return The default value to use for all {@link BuildMatcher#destinationPackage()} values.
     */
    String defaultDestinationPackage() default "";
}
