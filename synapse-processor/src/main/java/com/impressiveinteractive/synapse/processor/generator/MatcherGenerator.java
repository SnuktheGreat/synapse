package com.impressiveinteractive.synapse.processor.generator;

import com.impressiveinteractive.synapse.processor.pojo.MatcherPojo;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import static java.util.Objects.requireNonNull;

/**
 * Utility class used to generate implementations of the
 * {@link com.impressiveinteractive.synapse.test.ChainableMatcher}.
 */
public class MatcherGenerator {
    private MatcherGenerator() {
        throw new AssertionError("Illegal private constructor call.");
    }

    /**
     * Generate matcher source code from the given pojo object.
     *
     * @param matcher The given pojo object.
     * @return The source code.
     */
    public static String generateSourceCode(MatcherPojo matcher) {
        requireNonNull(matcher);
        STGroupFile group = createSTGroupFile("MatcherGenerator.stg");
        ST main = group.getInstanceOf("main");
        main.add("matcher", matcher);
        return main.render();
    }

    private static STGroupFile createSTGroupFile(String resourceName) {
        return new STGroupFile(MatcherGenerator.class.getResource(resourceName), "UTF-8", '<', '>');
    }
}
