package com.impressiveinteractive.synapse.lambda;

import com.impressiveinteractive.synapse.exception.Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Lambdas {

    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("\\((?<arguments>(L[\\w_/]+;)*)\\)(?<return>(L[\\w_/]+;|\\w))");
    private static final Pattern TYPE_PATTERN = Pattern.compile("L(?<type>[\\w_/]+);");

    private Lambdas() {
        throw new AssertionError("Calling private constructor. No instances should be created from this class.");
    }

    public static Class<?> getRawReturnType(SerializableLambda lambda) {
        Matcher signatureMatcher = matchSignature(lambda);

        String returnType = signatureMatcher.group("return");
        Matcher typeMatcher = TYPE_PATTERN.matcher(returnType);
        if (!typeMatcher.matches()) {
            throw Exceptions.format(IllegalArgumentException::new,
                    "Could extract type from {} ({}).", lambda, returnType);
        }
        try {
            return Class.forName(typeMatcher.group("type").replace('/', '.'));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Impossible! Could not get return type from the fully classified classname.", e);
        }
    }
    public static List<Class<?>> getRawArgumentTypes(SerializableLambda lambda) {
        Matcher signatureMatcher = matchSignature(lambda);
        String arguments = signatureMatcher.group("arguments");
        Matcher typeMatcher = TYPE_PATTERN.matcher(arguments);
        List<Class<?>> argumentTypes = new ArrayList<>();
        while (typeMatcher.find()) {
            try {
                argumentTypes.add(Class.forName(typeMatcher.group("type").replace('/', '.')));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Impossible! Could not get argument type from the fully classified classname.", e);
            }
        }
        return argumentTypes;
    }
    private static Matcher matchSignature(SerializableLambda lambda) {
        String methodType = lambda.serialized().getInstantiatedMethodType();
        Matcher signatureMatcher = SIGNATURE_PATTERN.matcher(methodType);
        if (!signatureMatcher.matches()) {
            throw Exceptions.format(IllegalArgumentException::new,
                    "Could not find signature on {} ({}).", lambda, methodType);
        }
        return signatureMatcher;
    }
}