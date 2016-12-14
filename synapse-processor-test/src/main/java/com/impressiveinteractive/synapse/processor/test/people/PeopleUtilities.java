package com.impressiveinteractive.synapse.processor.test.people;

import java.util.List;
import java.util.stream.Collectors;

public class PeopleUtilities {
    public static String fullName(Person person) {
        return person.getFirstName() + " " + person.getSurName();
    }

    public static List<Person> men(Group group) {
        return group.getAll().stream()
                .filter(p -> p.getGender() == Person.Gender.MALE)
                .collect(Collectors.toList());
    }

    public static List<Person> women(Group group) {
        return group.getAll().stream()
                .filter(p -> p.getGender() == Person.Gender.FEMALE)
                .collect(Collectors.toList());
    }
}
