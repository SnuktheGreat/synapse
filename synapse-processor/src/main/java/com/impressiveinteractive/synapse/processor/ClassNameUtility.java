package com.impressiveinteractive.synapse.processor;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * The {@link ClassNameUtility} is used to analyze {@link String} class names and extract parts out of it.
 */
public class ClassNameUtility {
    /**
     * Pattern used to extract the simple class name ($2) from a fully qualified class name ($0).
     */
    private static final Pattern IMPORT_PATTERN = Pattern.compile("(\\p{Ll}+\\.)+(\\p{Lu}\\w*)");

    /**
     * Pattern used to extract the package ($1) and class name ($3) from a fully qualified class name ($0).
     */
    private static final Pattern PACKAGE_CLASS_PATTERN =
            Pattern.compile("([\\p{Ll}0-9]+(\\.[\\p{Ll}0-9]+)*)\\.(\\p{Lu}[\\p{L}0-9]*(\\.\\p{Lu}[\\p{L}0-9]*)*)");

    private ClassNameUtility() {
        throw new AssertionError("Illegal private constructor call.");
    }

    /**
     * Extract all class names that need to be imported from a fully qualified class name with optional generics. Some
     * examples:
     * <ul>
     * <li>
     * com.impressiveinteractive.percy.Serialized
     * <ul>
     * <li>com.impressiveinteractive.percy.Serialized</li>
     * </ul>
     * </li>
     * <li>
     * java.util.Map&lt;java.util.String, com.impressiveinteractive.percy.Serialized&gt;
     * <ul>
     * <li>java.util.Map</li>
     * <li>java.util.String</li>
     * <li>com.impressiveinteractive.percy.Serialized</li>
     * </ul>
     * </li>
     * <li>
     * double
     * <ul>
     * <li><em>empty</em></li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param type String of fully classified class name with optional generics
     * @return All fully classified class names that can be imported
     */
    public static Set<String> extractImports(String type) {
        Matcher matcher = IMPORT_PATTERN.matcher(requireNonNull(type));

        Set<String> imports = new HashSet<>();
        while (matcher.find()) {
            imports.add(matcher.group());
        }
        return imports;
    }

    /**
     * Retrieve the simplified class name from a fully qualified one. For inner classes both the parent class name and
     * inner class name are returned. Examples:
     * <ul>
     * <li>com.java.util.Map -> Map</li>
     * <li>com.java.util.Map.Entry -> Map.Entry</li>
     * </ul>
     *
     * @param type The fully classified class name
     * @return The simplified class name
     */
    public static String simplify(String type) {
        return IMPORT_PATTERN.matcher(requireNonNull(type)).replaceAll("$2");
    }

    /**
     * Extract the package name from a fully classified class name.
     *
     * @param qualifiedName Fully classified class name
     * @return The package name
     */
    public static String extractPackage(String qualifiedName) {
        return getPackageClassMatcher(requireNonNull(qualifiedName)).group(1);
    }

    /**
     * Extract the simple class name from a fully classified class name.
     *
     * @param qualifiedName Fully classified class name
     * @return The simple class name
     */
    public static String extractClass(String qualifiedName) {
        return getPackageClassMatcher(requireNonNull(qualifiedName)).group(3);
    }

    private static Matcher getPackageClassMatcher(String qualifiedName) {
        Matcher matcher = PACKAGE_CLASS_PATTERN.matcher(qualifiedName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Argument given is not a qualified class name: " + qualifiedName);
        }
        return matcher;
    }
}
