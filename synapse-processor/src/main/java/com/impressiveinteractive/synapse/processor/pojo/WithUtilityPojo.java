package com.impressiveinteractive.synapse.processor.pojo;

import org.hamcrest.Matcher;

import java.util.Set;
import java.util.TreeSet;

import static com.impressiveinteractive.synapse.processor.ClassNameUtility.extractImports;
import static com.impressiveinteractive.synapse.processor.ClassNameUtility.simplify;

/**
 * Contains all data to generate a {@code with*} utility method.
 */
public class WithUtilityPojo implements ImportHolder {

    private final Set<String> imports = new TreeSet<>();

    private final String utilityType;
    private final String methodName;
    private final String propertyName;
    private final String returnType;

    public WithUtilityPojo(
            String utilityType, String methodName,
            String propertyName, String returnType) {
        this.utilityType = utilityType;
        this.methodName = methodName;
        this.propertyName = propertyName;
        this.returnType = returnType;

        imports.add(Matcher.class.getCanonicalName());
        imports.addAll(extractImports(utilityType));
        imports.addAll(extractImports(returnType));
    }

    @Override
    public Set<String> getImports() {
        return imports;
    }

    public String getUtilityType() {
        return utilityType;
    }

    public String getSimpleUtilityType() {
        return simplify(utilityType);
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

    public String getSimpleReturnType() {
        return simplify(returnType);
    }
}
