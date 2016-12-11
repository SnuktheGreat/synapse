package com.impressiveinteractive.synapse.processor.pojo;

import com.impressiveinteractive.synapse.test.ChainableMatcher;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.requireNonNull;

/**
 * Contains all data to generate a Hamcrest matcher for a destination type.
 */
public class MatcherPojo implements ImportHolder {

    private final Set<String> imports = new TreeSet<>();

    private final String packageName;
    private final String destinationName;
    private final List<WithGetterLikePojo> getterLikes = new ArrayList<>();
    private final List<WithUtilityPojo> utilities = new ArrayList<>();

    public MatcherPojo(String packageName, String destinationName) {
        this.packageName = packageName;
        this.destinationName = destinationName;

        imports.add(Generated.class.getCanonicalName());
        imports.add(packageName + "." + destinationName);
        imports.add(ChainableMatcher.class.getCanonicalName());
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

    public String getPackageName() {
        return packageName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getPropertyName() {
        return destinationName.substring(0, 1).toLowerCase() + destinationName.substring(1);
    }

    public List<WithGetterLikePojo> getGetterLikes() {
        return getterLikes;
    }

    public List<WithUtilityPojo> getUtilities() {
        return utilities;
    }
}
