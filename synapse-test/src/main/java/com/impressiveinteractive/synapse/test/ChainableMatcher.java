package com.impressiveinteractive.synapse.test;

import com.impressiveinteractive.synapse.lambda.SerializableFunction;
import com.impressiveinteractive.synapse.reflect.Typed;
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

/**
 * This Hamcrest {@link Matcher} is used to match custom method references on custom objects. It's basically a very
 * quick and intuitive way of writing a custom matcher. Example:
 * <pre>
 * assertThat(Person.name("Steve", "Jones").gender(Gender.MALE).age(43).awesome(true),
 *         is(ofType(Person.class)
 *                 .where(Person::getFirstName, is("Steve"))
 *                 .where(Person::getSurName, is("Jones"))
 *                 .where(Person::getGender, is(Gender.MALE))
 *                 .where(Person::getAge, is(43))
 *                 .where(Person::isAwesome, is(true))));
 * </pre>
 * <p>
 * See the test class for more examples.
 *
 * @param <T> The type of the object being matched.
 */
public class ChainableMatcher<T> extends BaseMatcher<T> {

    private static final String LAMBDA_NAME = "<lambda>";

    private final List<FieldMatcher<?>> fieldMatchers = new ArrayList<>();
    private final Typed<T> type;

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param cls The given (simple) type.
     * @param <T> The type of the object being matched.
     * @return A new {@link ChainableMatcher} for the given type.
     */
    public static <T> ChainableMatcher<T> ofType(Class<T> cls) {
        return new ChainableMatcher<>(cls);
    }

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param type The given type.
     * @param <T>  The type of the object being matched.
     * @return A new {@link ChainableMatcher} for the given type.
     */
    public static <T> ChainableMatcher<T> ofType(Typed<T> type) {
        return new ChainableMatcher<>(type);
    }

    /**
     * Create a new {@link FieldMapper} for the given value extractor. Note that the given function
     * <strong>must</strong> be a method reference on a class if type {@link T} and must be getter-like (have 0
     * arguments and a return value). A description for the given method reference is inferred.
     *
     * @param valueExtractor Method reference to a getter-like method.
     * @param <T>            The type of the object being matched.
     * @param <V>            The initial value from the given method reference.
     * @return The {@link FieldMapper} to map from type {@link V}.
     */
    public static <T, V> FieldMapper<T, V, V> map(SerializableFunction<T, V> valueExtractor) {
        return new FieldMapper<>(new Field<>(requireNonNull(valueExtractor)));
    }

    /**
     * Create a new {@link FieldMapper} for the given value extractor. Note that the given function
     * <strong>must</strong> be a method reference on a class if type {@link T} and must be getter-like (have 0
     * arguments and a return value). A description for the given method reference is given.
     *
     * @param description    A text description of the method reference.
     * @param valueExtractor Method reference to a getter-like method.
     * @param <T>            The type of the object being matched.
     * @param <V>            The initial value from the given method reference.
     * @return The {@link FieldMapper} to map from type {@link V}.
     */
    public static <T, V> FieldMapper<T, V, V> map(String description, SerializableFunction<T, V> valueExtractor) {
        return new FieldMapper<>(new Field<>(requireNonNull(valueExtractor), description));
    }

    /**
     * Describes the given function, taking into consideration it is used with the {@link ChainableMatcher}. The given
     * function must either be a getter or a single argument get method. This can be a 0 argument lambda as well. In the
     * first case the field name will be inferred from the method name and returned. In the second case the getter name
     * will be returned in full with accompanying {@code ()} brackets. If it is a lambda, the value for
     * {@link #LAMBDA_NAME} will be returned.
     * <p/>
     * <strong>Package protected for easy testing.</strong>
     *
     * @param function The function to describe.
     * @param <T>      The input type of the function.
     * @param <R>      The result type of the function.
     * @return A small string representation describing the function.
     */
    static <T, R> String describe(SerializableFunction<T, R> function) {
        return extractGetterLikeName(function.serialized());
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
            return LAMBDA_NAME + "(" + appliedTo + ")";
        } else if (kind == 6 || serialized.getCapturedArgCount() == 1) {
            String className = serialized.getImplClass();
            int lastSlash = className.lastIndexOf('/');
            return className.substring(lastSlash + 1) + "." + serialized.getImplMethodName()
                    + "(" + appliedTo + ")";
        }
        throw new IllegalArgumentException("Unknown type of SerializableFunction.");
    }

    private static String extractGetterLikeName(SerializedLambda serialized) {
        String name;
        String methodName = serialized.getImplMethodName();
        if (isGetterLike(serialized)) {
            if (isGetter(serialized)) {
                String nameWithCapitalStart = methodName.replaceAll("^(get|is)([A-Z].*$)", "$2");
                name = nameWithCapitalStart.substring(0, 1).toLowerCase()
                        + nameWithCapitalStart.substring(1);
            } else {
                name = methodName + "()";
            }
        } else if (methodName.startsWith("lambda")) {
            return LAMBDA_NAME;
        } else {
            name = methodName;
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
        this(Typed.of(cls));
    }

    /**
     * Create a new {@link ChainableMatcher} for the given type.
     *
     * @param type The given type.
     */
    public ChainableMatcher(Typed<T> type) {
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(Object o) {
        if (type.getRawType().isInstance(o)) {
            T instance = (T) o;
            return fieldMatchers.stream()
                    .allMatch(matcher -> matcher.matches(instance));
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
                String fieldName = subMatcher.mapper.getDescription();
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
     * Match the result of the given method reference with the given matcher. Note that the given function
     * <strong>must</strong> be a method reference on a class if type {@link T} and must be getter-like (have 0
     * arguments and a return value). A description for the given method reference is inferred.
     *
     * @param valueExtractor Method reference to a getter-like method.
     * @param matcher        The given matcher.
     * @param <V>            The value type for the described field.
     * @return This {@link ChainableMatcher} for further chaining.
     */
    public <V> ChainableMatcher<T> where(SerializableFunction<T, V> valueExtractor, Matcher<? super V> matcher) {
        requireNonNull(valueExtractor);
        return where(describe(valueExtractor), valueExtractor, matcher);
    }

    /**
     * Match the result of the given method reference with the given matcher. Note that the given function
     * <strong>must</strong> be a method reference on a class if type {@link T} and must be getter-like (have 0
     * arguments and a return value). A description for the given method reference is given.
     *
     * @param description    A text description of the method reference.
     * @param valueExtractor Method reference to a getter-like method.
     * @param matcher        The given matcher.
     * @param <V>            The value type for the described field.
     * @return This {@link ChainableMatcher} for further chaining.
     */
    public <V> ChainableMatcher<T> where(String description, SerializableFunction<T, V> valueExtractor, Matcher<? super V> matcher) {
        Field<T, V> field = new Field<>(requireNonNull(valueExtractor), description);
        FieldMapper<T, V, V> mapper = new FieldMapper<>(field);
        return where(mapper, matcher);
    }

    /**
     * Match the result of the given {@link FieldMapper} with the given matcher.
     *
     * @param mapper  The {@link FieldMapper}.
     * @param matcher The given matcher.
     * @param <V1>    The initial value type from the field mapper.
     * @param <V2>    The eventual mapped value type from the field mapper.
     * @return This {@link ChainableMatcher} for further chaining.
     * @see #map(SerializableFunction)
     * @see #map(String, SerializableFunction)
     */
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

    /**
     * Used to chain transformations together to convert the value of the original method reference to the value to the
     * <em>actual</em> to test against. Example:
     * <pre>
     * map(People::getList)
     *         .to("get(0)", list -&gt; list.get(0))
     *         .to(Person::getFirstName)
     * </pre>
     *
     * @param <T> The original type from which the initial value is extracted.
     * @param <V> The type of the initial value.
     * @param <R> The type returned when this mapper completes.
     * @see #map(SerializableFunction)
     * @see #map(String, SerializableFunction)
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
         * Map the current return type {@link R} to the new return type {@link R2} and description the transformation.
         *
         * @param description    The description of the transformation. The Lambda {@code list -&gt; list.get(0)} could
         *                       be described as "get(0)" for example.
         * @param transformation The function used to convert from {@link R} to {@link R2}.
         * @param <R2>           The new type returned when this mapper completes.
         * @return The mapper that extracts the initial value of type {@link V} from {@link T} and converts it to
         * {@link R2} using all given transformations.
         */
        @SuppressWarnings("unchecked")
        public <R2> FieldMapper<T, V, R2> to(String description, SerializableFunction<R, R2> transformation) {
            fields.add(new Field<>(requireNonNull(transformation), description));
            return (FieldMapper<T, V, R2>) this;
        }

        private String getDescription() {
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

        private final String description;
        private final SerializableFunction<T, V> valueExtractor;

        private Field(SerializableFunction<T, V> valueExtractor) {
            this(valueExtractor, null);
        }

        private Field(SerializableFunction<T, V> valueExtractor, String description) {
            this.description = description;
            this.valueExtractor = valueExtractor;
        }

        private V extractValue(T instance) {
            return valueExtractor.apply(instance);
        }

        private String describe() {
            return description != null ? description : ChainableMatcher.describe(valueExtractor);
        }

        private String describe(String appendTo) {
            return description != null
                    ? appendTo + "." + description
                    : ChainableMatcher.describe(valueExtractor, appendTo);
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
                    .appendText(mapper.getDescription())
                    .appendText(" ")
                    .appendDescriptionOf(matcher);
        }
    }
}
