package com.impressiveinteractive.synapse.processor.pojo;

import com.impressiveinteractive.synapse.lambda.SerializableFunction;
import com.impressiveinteractive.synapse.reflect.Typed;
import com.impressiveinteractive.synapse.test.ChainableMatcher;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Contains all data to generate a Hamcrest matcher for a destination type.
 */
public class MatcherPojo implements ImportHolder {

    private final Set<String> imports = new TreeSet<>();

    private final String pojoName;
    private final List<GenericPojo> generics;
    private final String destinationPackage;
    private final String destinationName;
    private final String staticMethodName;
    private final List<WithGetterLikePojo> getterLikes = new ArrayList<>();
    private final List<WithUtilityPojo> utilities = new ArrayList<>();

    public MatcherPojo(
            String pojoCanonicalName,
            String pojoName,
            List<GenericPojo> generics,
            String destinationPackage,
            String destinationName,
            String staticMethodName) {
        this.pojoName = pojoName;
        this.generics = generics;
        this.destinationPackage = destinationPackage;
        this.destinationName = destinationName;
        this.staticMethodName = staticMethodName;

        imports.add(Generated.class.getCanonicalName());
        imports.add(SerializableFunction.class.getCanonicalName());
        imports.add(pojoCanonicalName);
        imports.add(ChainableMatcher.class.getCanonicalName());
        if (!generics.isEmpty()) {
            imports.add(Typed.class.getCanonicalName());
            generics.forEach(generic -> imports.addAll(generic.getImports()));
        }
    }

    public void addGetterLike(WithGetterLikePojo pojo) {
        requireNonNull(pojo);
        imports.addAll(pojo.getImports());
        getterLikes.add(pojo);
    }

    public void addUtility(WithUtilityPojo pojo) {
        requireNonNull(pojo);
        imports.addAll(pojo.getImports());
        utilities.add(pojo);
    }

    @Override
    public Set<String> getImports() {
        return imports;
    }

    public String getPojoName() {
        return pojoName;
    }

    public String getDestinationPackage() {
        return destinationPackage;
    }

    public String getDestinationName() {
        return destinationName;
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

    public String getStaticMethodName() {
        return staticMethodName;
    }

    public String getPropertyName() {
        return pojoName.substring(0, 1).toLowerCase() + pojoName.substring(1);
    }

    public List<WithGetterLikePojo> getGetterLikes() {
        return getterLikes;
    }

    public List<WithUtilityPojo> getUtilities() {
        return utilities;
    }
}
