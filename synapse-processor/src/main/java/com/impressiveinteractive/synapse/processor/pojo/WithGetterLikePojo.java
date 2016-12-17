package com.impressiveinteractive.synapse.processor.pojo;

import com.impressiveinteractive.synapse.lambda.SerializableFunction;
import com.impressiveinteractive.synapse.processor.ClassNameUtility;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.impressiveinteractive.synapse.processor.ClassNameUtility.simplify;

/**
 * Contains all data to generate a {@code with*} getter like method.
 */
public class WithGetterLikePojo implements ImportHolder {

    private final Set<String> imports = new TreeSet<>();

    private final String methodName;
    private final List<GenericPojo> generics;
    private final String propertyName;
    private final String returnType;
    private final boolean exceptional;

    public WithGetterLikePojo(String methodName, List<GenericPojo> generics, String propertyName, String returnType, boolean exceptional) {
        this.methodName = methodName;
        this.generics = generics;
        this.propertyName = propertyName;
        this.returnType = returnType;
        this.exceptional = exceptional;

        imports.add(Matcher.class.getCanonicalName());
        imports.addAll(ClassNameUtility.extractImports(returnType));
        generics.forEach(generic -> imports.addAll(generic.getImports()));
        if(exceptional) {
            imports.add(SerializableFunction.class.getCanonicalName());
            imports.add(RuntimeException.class.getCanonicalName());
            imports.add(Exception.class.getCanonicalName());
            imports.add(AssertionError.class.getCanonicalName());
        }
    }

    @Override
    public Set<String> getImports() {
        return imports;
    }

    public String getTypedGenerics() {
        return generics.isEmpty() ? null
                : "<" + generics.stream()
                .map(GenericPojo::getFullName)
                .collect(Collectors.joining(", ")) + ">";
    }

    public String getGenerics() {
        return generics.isEmpty() ? null
                : "<" + generics.stream()
                .map(GenericPojo::getName)
                .collect(Collectors.joining(", ")) + ">";
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getSimpleReturnType(){
        return simplify(returnType);
    }

    public boolean isExceptional() {
        return exceptional;
    }
}
