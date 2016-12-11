package com.impressiveinteractive.synapse.processor;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ClassNameUtilityTest {
    @Test
    public void testExtractImports() throws Exception {
        assertThat(ClassNameUtility.extractImports("com.impressiveinteractive.percy.Serialized"),
                containsInAnyOrder("com.impressiveinteractive.percy.Serialized"));

        assertThat(ClassNameUtility.extractImports(
                "java.util.Map<java.util.String, com.impressiveinteractive.percy.Serialized>"
        ), containsInAnyOrder(
                "java.util.Map",
                "java.util.String",
                "com.impressiveinteractive.percy.Serialized"));

        assertThat(ClassNameUtility.extractImports("double"),
                is(empty()));
    }

    @Test(expected = NullPointerException.class)
    public void testExtractImportsNPE() throws Exception {
        ClassNameUtility.extractImports(null);
    }

    @Test
    public void testSimplify() throws Exception {
        assertThat(ClassNameUtility.simplify("com.java.util.Map"),
                is(equalTo("Map")));
        assertThat(ClassNameUtility.simplify("com.java.util.Map.Entry"),
                is(equalTo("Map.Entry")));
    }

    @Test(expected = NullPointerException.class)
    public void testSimplifyNPE() throws Exception {
        ClassNameUtility.simplify(null);
    }

    @Test
    public void testExtractPackage() throws Exception {
        assertThat(ClassNameUtility.extractPackage("com.impressiveinteractive.percy.processor.ClassNameUtility"),
                is(equalTo("com.impressiveinteractive.percy.processor")));
    }

    @Test(expected = NullPointerException.class)
    public void testExtractPackageNPE() throws Exception {
        ClassNameUtility.extractPackage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractPackageFromNotFullyQualifiedName() throws Exception {
        ClassNameUtility.extractPackage("Not a fully classified class name");
    }

    @Test
    public void testExtractClass() throws Exception {
        assertThat(ClassNameUtility.extractClass("com.impressiveinteractive.percy.processor.ClassNameUtility"),
                is(equalTo("ClassNameUtility")));
    }

    @Test(expected = NullPointerException.class)
    public void testExtractClassNPE() throws Exception {
        ClassNameUtility.extractClass(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractClassFromNotFullyQualifiedName() throws Exception {
        ClassNameUtility.extractClass("Not a fully classified class name");
    }

    private Set<String> empty() {
        return Collections.emptySet();
    }
}
