package com.impressiveinteractive.synapse.processor.pojo;

import java.util.Collections;
import java.util.Set;

public class GenericPojo implements ImportHolder {
    private final String name;
    private final String upperBoundName;
    private final Set<String> imports;

    public GenericPojo(String name, String upperBoundName, String upperBoundType) {
        this.name = name;
        this.upperBoundName = upperBoundName;
        imports = upperBoundType == null ? Collections.emptySet() : Collections.singleton(upperBoundType);
    }

    @Override
    public Set<String> getImports() {
        return imports;
    }

    public String getName() {
        return name;
    }

    public String getUpperBoundName() {
        return upperBoundName;
    }

    public String getFullName() {
        return upperBoundName == null ? name : name + " extends " + upperBoundName;
    }
}
