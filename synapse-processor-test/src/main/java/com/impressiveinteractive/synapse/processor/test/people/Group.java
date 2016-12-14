package com.impressiveinteractive.synapse.processor.test.people;

import java.util.Arrays;
import java.util.List;

public class Group {

    private final List<Person> all;

    public static Group of(Person... persons) {
        return new Group(Arrays.asList(persons));
    }

    protected Group(List<Person> all) {
        this.all = all;
    }

    public List<Person> getAll() {
        return all;
    }

    public void dance() {
        all.forEach(Person::dance);
    }
}