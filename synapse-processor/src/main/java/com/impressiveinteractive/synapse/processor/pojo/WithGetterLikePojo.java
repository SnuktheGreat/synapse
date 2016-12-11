package com.impressiveinteractive.synapse.processor.pojo;

import com.impressiveinteractive.synapse.processor.ClassNameUtility;
import org.hamcrest.Matcher;

import java.util.Set;
import java.util.TreeSet;

import static com.impressiveinteractive.synapse.processor.ClassNameUtility.simplify;

/**
 * Contains all data to generate a {@code with*} getter like method.
 */
public class WithGetterLikePojo implements ImportHolder {

    private final Set<String> imports = new TreeSet<>();

    private final String destinationName;
    private final String methodName;
    private final String propertyName;
    private final String returnType;

    public WithGetterLikePojo(String destinationName, String methodName, String propertyName, String returnType) {
        this.destinationName = destinationName;
        this.methodName = methodName;
        this.propertyName = propertyName;
        this.returnType = returnType;

        imports.add(Matcher.class.getCanonicalName());
        imports.addAll(ClassNameUtility.extractImports(returnType));
    }

    @Override
    public Set<String> getImports() {
        return imports;
    }

    public String getDestinationName() {
        return destinationName;
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
}
