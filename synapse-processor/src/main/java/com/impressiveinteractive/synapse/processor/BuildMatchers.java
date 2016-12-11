package com.impressiveinteractive.synapse.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Container of {@link BuildMatcher} annotations, if more than one Hamcrest matcher should be generated.
 */
@Target(ElementType.TYPE)
public @interface BuildMatchers {
    BuildMatcher[] value();
}
