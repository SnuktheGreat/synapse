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

    private final List<FieldMatcher<?>> fieldMatchers = new ArrayList<>();
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

    /**
     * Describes the given function, taking into consideration it is used with the {@link ChainableMatcher}. The given
     * function must either be a getter or a single argument get method. In the first case the field name will be
     * inferred from the method name and returned. In the second case the getter name will be returned in full with
     * accompanying {@code ()} brackets.
     * <p/>
     * <strong>Package protected for easy testing.</strong>
     *
     * @param function The function to describe.
     * @param <T>      The input type of the function.
     * @param <R>      The result type of the function.
     * @return A small string representation describing the function.
     */
    static <T, R> String describe(SerializableFunction<T, R> function) {
        SerializedLambda serialized = function.serialized();
        if (!isGetterLike(serialized)) {
            throw new IllegalArgumentException("The given lambda is not getter like.");
        }
        return extractGetterLikeName(serialized);
    }

    /**
     * Describe a given function in the context that it is being applied to the given <em>appliedTo</em> argument. If
     * the given function is a getter like method, it will produce "appliedTo.field" or "appliedTo.getterLikeMethod()";
     * If the given function is a lambda, it will produce "&lt;lambda&gt;(appliedTo)"; If the given function is a single
     * argument method, it will produce "ClassName.methodName(<em>appliedTo</em>)"; Other function types should not be
     * given.
     * <p/>
     * <strong>Package protected for easy testing.</strong>
     */
    static <T, R> String describe(SerializableFunction<T, R> valueExtractor, String appliedTo) {
        requireNonNull(appliedTo);

        SerializedLambda serialized = valueExtractor.serialized();
        int kind = serialized.getImplMethodKind(); // See MethodHandleInfo for more information about method kinds
        if (isGetterLike(serialized)) {
            return appliedTo + "." + extractGetterLikeName(serialized);
        } else if (kind == 6 && serialized.getImplMethodName().startsWith("lambda")) {
            return "<lambda>(" + appliedTo + ")";
        } else if (kind == 6 || serialized.getCapturedArgCount() == 1) {
            String className = serialized.getImplClass();
            int lastSlash = className.lastIndexOf('/');
            return className.substring(lastSlash + 1, className.length()) + "." + serialized.getImplMethodName() + "(" + appliedTo + ")";
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
            return !fieldMatchers.stream()
                    .filter(matcher -> !matcher.matches(instance))
                    .findAny()
                    .isPresent();
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("of type ").appendText(type.getRawType().getSimpleName());
        fieldMatchers.forEach(field -> field.describeTo(description));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void describeMismatch(Object o, Description description) {
        if (type.getRawType().isInstance(o)) {
            T instance = (T) o;
            List<FieldMatcher<?>> misMatched = fieldMatchers.stream()
                    .filter(matcher -> !matcher.matches(instance))
                    .collect(Collectors.toList());
            description.appendText("has unexpected value for:\n");
            for (FieldMatcher<?> subMatcher : misMatched) {
                String fieldName = subMatcher.mapper.getName();
                Object fieldValue = subMatcher.mapper.extractValue(instance);
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
    public <V> ChainableMatcher<T> where(SerializableFunction<T, V> methodReference, Matcher<? super V> matcher) {
        requireNonNull(methodReference);
        return where(describe(methodReference), methodReference, matcher);
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
        Field<T, V> field = new Field<>(requireNonNull(valueExtractor), name);
        FieldMapper<T, V, V> mapper = new FieldMapper<>(field);
        return where(mapper, matcher);
    }

    public <V1, V2> ChainableMatcher<T> where(FieldMapper<T, V1, V2> mapper, Matcher<? super V2> matcher) {
        fieldMatchers.add(new FieldMatcher<>(requireNonNull(mapper), requireNonNull(matcher)));
        return ChainableMatcher.this;
    }

    private String describeMismatch(String fieldName, Object fieldValue, FieldMatcher<?> subMatcher) {
        StringDescription subMatcherDescription = new StringDescription();
        subMatcher.matcher.describeMismatch(fieldValue, subMatcherDescription);
        String unIndented = fieldName + " " + subMatcherDescription.toString();
        String[] unIndentedLines = unIndented.split("\\n+");
        return Arrays.stream(unIndentedLines)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> "\t" + line)
                .collect(Collectors.joining("\n"));
    }

    public static <T, V> FieldMapper<T, V, V> map(SerializableFunction<T, V> function) {
        return new FieldMapper<>(new Field<>(requireNonNull(function)));
    }

    public static <T, V> FieldMapper<T, V, V> map(String name, SerializableFunction<T, V> function) {
        return new FieldMapper<>(new Field<>(requireNonNull(function), name));
    }

    /**
     * Used to chain transformations together to convert the value of the original method reference to the value to the
     * <em>actual</em> to test against. Example:
     * <pre>
     * map(People::getList)
     *         .to("get(0)", list -> list.get(0))
     *         .to(Person::getFirstName)
     * </pre>
     *
     * @param <T> The original type from which the initial value is extracted.
     * @param <V> The type of the initial value.
     * @param <R> The type returned when this mapper completes.
     */
    public static final class FieldMapper<T, V, R> {

        private final Field<T, V> original;
        private final List<Field> fields = new ArrayList<>();

        private FieldMapper(Field<T, V> original) {
            this.original = original;
        }

        /**
         * Map the current return type {@link R} to the new return type {@link R2}.
         *
         * @param transformation The function used to convert from {@link R} to {@link R2}.
         * @param <R2>           The new type returned when this mapper completes.
         * @return The mapper that extracts the initial value of type {@link V} from {@link T} and converts it to
         * {@link R2} using all given transformations.
         */
        public <R2> FieldMapper<T, V, R2> to(SerializableFunction<R, R2> transformation) {
            return to(null, transformation);
        }

        /**
         * Map the current return type {@link R} to the new return type {@link R2} and name the transformation.
         *
         * @param name           The name of the result. Should describe the transformation. The Lambda
         *                       {@code list -> list.get(0)} could be described as "get(0)" for example.
         * @param transformation The function used to convert from {@link R} to {@link R2}.
         * @param <R2>           The new type returned when this mapper completes.
         * @return The mapper that extracts the initial value of type {@link V} from {@link T} and converts it to
         * {@link R2} using all given transformations.
         */
        @SuppressWarnings("unchecked")
        public <R2> FieldMapper<T, V, R2> to(String name, SerializableFunction<R, R2> transformation) {
            fields.add(new Field<>(requireNonNull(transformation), name));
            return (FieldMapper<T, V, R2>) this;
        }

        private String getName() {
            String candidate = original.describe();
            for (Field field : fields) {
                candidate = field.describe(candidate);
            }
            return candidate;
        }

        @SuppressWarnings("unchecked")
        private R extractValue(T instance) {
            Object candidate = original.extractValue(instance);
            for (Field field : fields) {
                candidate = field.extractValue(candidate);
            }
            return (R) candidate;
        }
    }

    private static final class Field<T, V> {

        private final String name;
        private final SerializableFunction<T, V> valueExtractor;

        private Field(SerializableFunction<T, V> valueExtractor) {
            this(valueExtractor, null);
        }

        private Field(SerializableFunction<T, V> valueExtractor, String name) {
            this.name = name;
            this.valueExtractor = valueExtractor;
        }

        private V extractValue(T instance) {
            return valueExtractor.apply(instance);
        }

        private String describe() {
            return name != null ? name : ChainableMatcher.describe(valueExtractor);
        }

        private String describe(String appendTo) {
            return name != null ? appendTo + "." + name : ChainableMatcher.describe(valueExtractor, appendTo);
        }
    }

    private final class FieldMatcher<V> implements SelfDescribing {

        private final FieldMapper<T, ?, V> mapper;
        private final Matcher<? super V> matcher;

        private FieldMatcher(FieldMapper<T, ?, V> mapper, Matcher<? super V> matcher) {
            this.mapper = mapper;
            this.matcher = matcher;
        }

        private boolean matches(T instance) {
            V value = mapper.extractValue(instance);
            return matcher.matches(value);
        }

        @Override
        public void describeTo(Description description) {
            description
                    .appendText(" with ")
                    .appendText(mapper.getName())
                    .appendText(" ")
                    .appendDescriptionOf(matcher);
        }
    }
}
