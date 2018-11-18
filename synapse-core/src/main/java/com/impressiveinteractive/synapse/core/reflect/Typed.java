package com.impressiveinteractive.synapse.core.reflect;

import com.impressiveinteractive.synapse.core.exception.Exceptions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Used to capture generic types. To use it you need to extend {@link Typed} with the actual concrete type that should
 * be captured. Example:
 *
 * <pre>
 * Typed&lt;List&lt;String&gt;&gt; listOfStrings = new Typed&lt;List&lt;String&gt;&gt;() {};
 * </pre>
 *
 * Based on the TypeToken of Guava, but heavily simplified. Consider using Guava's TypeToken for more complex
 * operations.
 *
 * @param <T> The captured type.
 */
public abstract class Typed<T> {
    private final Type type;

    /**
     * Create a concrete {@link Typed} instance for the given class.
     *
     * @param cls The given class
     * @param <T> The type this class represents
     * @return A concrete {@link Typed} instance for the given class.
     */
    public static <T> Typed<T> of(Class<T> cls) {
        return new TypedFromClass<>(cls);
    }

    /**
     * Extracts the type from the instantiating type. See example in class JavaDoc.
     */
    protected Typed() {
        Type superType = getClass().getGenericSuperclass();
        if (!(superType instanceof ParameterizedType))
            throw Exceptions.format(IllegalArgumentException::new, "Type {} is not a parameterized type.", superType);
        Type candidate = ((ParameterizedType) superType).getActualTypeArguments()[0];
        if (!(candidate instanceof Class) && !(candidate instanceof ParameterizedType))
            throw Exceptions.format(IllegalArgumentException::new, "Type {} is not concrete.", candidate);
        this.type = candidate;
    }

    protected Typed(Type type) {
        this.type = type;
    }

    /**
     * @return The captured type.
     */
    public final Type getType() {
        return type;
    }

    /**
     * @return The raw {@link Class} representation of the captured type.
     */
    @SuppressWarnings("unchecked")
    public final Class<? super T> getRawType() {
        return (Class<? super T>) (type instanceof Class ? type : ((ParameterizedType) type).getRawType());
    }

    private static class TypedFromClass<T> extends Typed<T> {
        private TypedFromClass(Class<T> cls) {
            super(cls);
        }
    }
}
