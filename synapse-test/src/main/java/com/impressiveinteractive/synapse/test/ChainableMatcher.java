package com.impressiveinteractive.synapse.test;

import com.google.common.reflect.TypeToken;
import com.impressiveinteractive.synapse.lambda.SerializableFunction;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.StringDescription;

import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ChainableMatcher<T> extends BaseMatcher<T> {

    private final List<ExtractorMatcher<?>> extractorMatchers = new ArrayList<>();
    private final TypeToken<T> type;

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param cls The given (simple) type.
     * @param <T> The type.
     * @return A new {@link ChainableMatcher} for the given type.
     */
    public static <T> ChainableMatcher<T> ofType(Class<T> cls) {
        return new ChainableMatcher<>(cls);
    }

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param type The given type.
     * @param <T>  The type.
     * @return A new {@link ChainableMatcher} for the given type.
     */
    public static <T> ChainableMatcher<T> ofType(TypeToken<T> type) {
        return new ChainableMatcher<>(type);
    }

    static <T, R> String describe(SerializableFunction<T, R> valueExtractor) {
        SerializedLambda serialized = valueExtractor.serialized();
        if (!isGetterLike(serialized)) {
            throw new IllegalArgumentException("The given lambda is not getter like.");
        }
        return extractGetterLikeName(serialized);
    }

    /**
     * Describe a given value extraction function. In the case of a getter the field name will be inferred from the method name and
     * returned; In the case of a zero argument method reference the method will be returned; In the case of another method reference idk
     * yet; In all other cases we magic something up.
     * <p>
     * <strong>Package protected for easy testing.</strong>
     */
    static <T, R> String describe(SerializableFunction<T, R> valueExtractor, String appendTo) {
        requireNonNull(appendTo, "appendTo can not be null.");

        SerializedLambda serialized = valueExtractor.serialized();
        int kind = serialized.getImplMethodKind(); // See MethodHandleInfo for more information about method kinds
        if (isGetterLike(serialized)) {
            return appendTo + "." + extractGetterLikeName(serialized);
        } else if (kind == 6 && serialized.getImplMethodName().startsWith("lambda")) {
            return "<lambda>(" + appendTo + ")";
        } else if (kind == 6 || serialized.getCapturedArgCount() == 1) {
            String className = serialized.getImplClass();
            int lastSlash = className.lastIndexOf('/');
            return className.substring(lastSlash + 1, className.length()) + "." + serialized.getImplMethodName() + "(" + appendTo + ")";
        }
        throw new IllegalArgumentException("Unknown type of SerializableFunction.");
    }

    private static String extractGetterLikeName(SerializedLambda serialized) {
        String name;
        if (isGetter(serialized)) {
            String nameWithCapitalStart = serialized.getImplMethodName().replaceAll("^(get|is)([A-Z].*$)", "$2");
            name = nameWithCapitalStart.substring(0, 1).toLowerCase()
                    + nameWithCapitalStart.substring(1, nameWithCapitalStart.length());
        } else {
            name = serialized.getImplMethodName() + "()";
        }
        return name;
    }

    private static boolean isGetterLike(SerializedLambda serialized) {
        int kind = serialized.getImplMethodKind();
        return (kind == 5 || kind == 7 || kind == 9) && serialized.getCapturedArgCount() == 0;
    }

    private static boolean isGetter(SerializedLambda serialized) {
        return isGetterLike(serialized) && serialized.getImplMethodName().matches("^(get|is)[A-Z].*$");
    }

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param cls The given (simple) type.
     */
    public ChainableMatcher(Class<T> cls) {
        this(TypeToken.of(cls));
    }

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param type The given type.
     */
    public ChainableMatcher(TypeToken<T> type) {
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(Object o) {
        if (type.getRawType().isInstance(o)) {
            T instance = (T) o;
            return !extractorMatchers.stream()
                    .filter(matcher -> !matcher.matches(instance))
                    .findAny()
                    .isPresent();
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("of type ").appendText(type.getRawType().getSimpleName());
        extractorMatchers.forEach(field -> field.describeTo(description));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void describeMismatch(Object o, Description description) {
        if (type.getRawType().isInstance(o)) {
            T instance = (T) o;
            List<ExtractorMatcher<?>> misMatched = extractorMatchers.stream()
                    .filter(matcher -> !matcher.matches(instance))
                    .collect(Collectors.toList());
            description.appendText("has unexpected value for:\n");
            for (ExtractorMatcher<?> subMatcher : misMatched) {
                String fieldName = subMatcher.extractor.getName();
                Object fieldValue = subMatcher.extractor.extractValue(instance);
                description
                        .appendText(describeMismatch(fieldName, fieldValue, subMatcher))
                        .appendText("\n\t\texpecting")
                        .appendDescriptionOf(subMatcher)
                        .appendText("\n");
            }
        } else {
            description
                    .appendText("of unexpected type ")
                    .appendText(o.getClass().getCanonicalName());
        }
    }

    /**
     * Apply the given matcher for the given method reference . Note that this function <strong>must</strong> be a method reference to a
     * getter. TODO more docs
     *
     * @param methodReference Method reference to a getter type method.
     * @param matcher         The given matcher.
     * @param <V>             The value type for the described field.
     * @return This {@link ChainableMatcher} for further chaining.
     */
    public <V> ChainableMatcher<T> where(
            SerializableFunction<T, V> methodReference, Matcher<? super V> matcher) {
        return where(describe(methodReference), methodReference, matcher);
    }

    public <V1, V2> ChainableMatcher<T> where(Mapper<T, V1, V2> mapper, Matcher<? super V2> matcher) {
        extractorMatchers.add(new ExtractorMatcher<>((MappingExtractor<T, V1, V2>) mapper, matcher));
        return ChainableMatcher.this;
    }

    /**
     * Apply the given matcher for a field with the given name and value extractor.
     *
     * @param name           The name describing the field.
     * @param valueExtractor The value extractor for the field.
     * @param matcher        The given matcher.
     * @param <V>            The value type for the described field.
     * @return This {@link ChainableMatcher} for further chaining.
     */
    public <V> ChainableMatcher<T> where(String name, SerializableFunction<T, V> valueExtractor, Matcher<? super V> matcher) {
        extractorMatchers.add(new ExtractorMatcher<>(new DirectExtractor<>(valueExtractor, name), matcher));
        return ChainableMatcher.this;
    }

    private String describeMismatch(String fieldName, Object fieldValue, ExtractorMatcher<?> subMatcher) {
        StringDescription subMatcherDescription = new StringDescription();
        subMatcher.matcher.describeMismatch(fieldValue, subMatcherDescription);
        String unIndented = fieldName + " " + subMatcherDescription.toString();
        String[] unIndentedLines = unIndented.split("\\n+");
        return Arrays.stream(unIndentedLines)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> "\t" + line)
                .collect(Collectors.joining("\n"));
    }

    public static <T, V> Mapper<T, V, V> map(SerializableFunction<T, V> function) {
        return new MappingExtractor<>(new DirectExtractor<>(function));
    }

    public static <T, V> Mapper<T, V, V> map(String name, SerializableFunction<T, V> function) {
        return new MappingExtractor<>(new DirectExtractor<>(function, name));
    }

    public interface Mapper<T, V, R> {
        <R2> Mapper<T, V, R2> to(SerializableFunction<R, R2> function);

        <R2> Mapper<T, V, R2> to(String name, SerializableFunction<R, R2> function);
    }

    private interface Extractor<T, V> {
        String getName();

        V extractValue(T instance);
    }

    private static class MappingExtractor<T, V, R> implements Mapper<T, V, R>, Extractor<T, R> {

        private final DirectExtractor<T, V> original;
        private final List<DirectExtractor> extractors = new ArrayList<>();

        private MappingExtractor(DirectExtractor<T, V> original) {
            this.original = original;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R2> Mapper<T, V, R2> to(SerializableFunction<R, R2> function) {
            return to(null, function);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R2> Mapper<T, V, R2> to(String name, SerializableFunction<R, R2> function) {
            extractors.add(new DirectExtractor<>(function, name));
            return (Mapper<T, V, R2>) this;
        }

        @Override
        public String getName() {
            String candidate = original.describe();
            for (DirectExtractor extractor : extractors) {
                candidate = extractor.describe(candidate);
            }
            return candidate;
        }

        @Override
        @SuppressWarnings("unchecked")
        public R extractValue(T instance) {
            Object candidate = original.extractValue(instance);
            for (DirectExtractor extractor : extractors) {
                candidate = extractor.extractValue(candidate);
            }
            return (R) candidate;
        }
    }

    private static final class DirectExtractor<T, V> implements Extractor<T, V> {

        private final String name;
        private final SerializableFunction<T, V> valueExtractor;

        private DirectExtractor(SerializableFunction<T, V> valueExtractor) {
            this(valueExtractor, null);
        }

        private DirectExtractor(SerializableFunction<T, V> valueExtractor, String name) {
            this.name = name;
            this.valueExtractor = requireNonNull(valueExtractor, "Value extractor required.");
        }

        @Override
        public String getName() {
            return describe();
        }

        @Override
        public V extractValue(T instance) {
            return valueExtractor.apply(instance);
        }

        private String describe() {
            return name != null ? name : ChainableMatcher.describe(valueExtractor);
        }

        private String describe(String appendTo) {
            return name != null ? appendTo + "." + name : ChainableMatcher.describe(valueExtractor, appendTo);
        }
    }

    private class ExtractorMatcher<V> implements SelfDescribing {

        private final Extractor<T, V> extractor;
        private final Matcher<? super V> matcher;

        private ExtractorMatcher(Extractor<T, V> extractor, Matcher<? super V> matcher) {
            this.extractor = extractor;
            this.matcher = matcher;
        }

        private boolean matches(T instance) {
            V value = extractor.extractValue(instance);
            return matcher.matches(value);
        }

        @Override
        public void describeTo(Description description) {
            description
                    .appendText(" with ")
                    .appendText(extractor.getName())
                    .appendText(" ")
                    .appendDescriptionOf(matcher);
        }
    }
}
