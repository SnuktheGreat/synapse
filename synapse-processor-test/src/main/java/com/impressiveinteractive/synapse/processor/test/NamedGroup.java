package com.impressiveinteractive.synapse.processor.test;

import java.util.Arrays;
import java.util.List;

public class NamedGroup extends Group {

    public static NamedGroup of(String name, Person... persons) {
        return new NamedGroup(name, Arrays.asList(persons));
    }

    private final String name;

    protected NamedGroup(String name, List<Person> all) {
        super(all);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
