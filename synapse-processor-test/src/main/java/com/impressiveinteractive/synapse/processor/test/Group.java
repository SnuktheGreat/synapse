package com.impressiveinteractive.synapse.processor.test;

import java.util.Arrays;
import java.util.List;

public class Group {

    private final List<Person> all;

    public static Group with(Person... persons) {
        return new Group(Arrays.asList(persons));
    }

    private Group(List<Person> all) {
        this.all = all;
    }

    public List<Person> getAll() {
        return all;
    }

    public void dance() {
        all.forEach(Person::dance);
    }
}