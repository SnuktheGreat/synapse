package com.impressiveinteractive.synapse.lambda;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * When extended, adds the {@link #serialized()} method to any {@link FunctionalInterface}. This method can be used to
 * extract information from a lambda at runtime.
 *
 * @see Lambdas
 */
public interface SerializableLambda extends Serializable {

    /**
     * @return A serialized version of this lambda
     */
    default SerializedLambda serialized() {
        try {
            Method replaceMethod = getClass().getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);
            return (SerializedLambda) replaceMethod.invoke(this);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Could not serialize Lambda.", e);
        }
    }
}
