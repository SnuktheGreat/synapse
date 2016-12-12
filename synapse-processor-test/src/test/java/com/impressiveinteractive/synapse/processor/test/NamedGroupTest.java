package com.impressiveinteractive.synapse.processor.test;

import com.impressiveinteractive.synapse.processor.BuildMatcher;
import com.impressiveinteractive.synapse.processor.BuildMatchers;
import org.junit.Test;

import static com.impressiveinteractive.synapse.processor.test.NamedGroupMatcher.namedGroup;
import static com.impressiveinteractive.synapse.processor.test.Person.Gender.FEMALE;
import static com.impressiveinteractive.synapse.processor.test.Person.Gender.MALE;
import static com.impressiveinteractive.synapse.processor.test.PersonMatcher.person;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@BuildMatchers({
        @BuildMatcher(Person.class),
        @BuildMatcher(pojo = NamedGroup.class, utilities = PeopleUtilities.class)})
public class NamedGroupTest {
    @Test
    public void testGeneratedMatcher() throws Exception {
        NamedGroup doctors = NamedGroup.of(
                "Doctors",
                Person.name("Gregory", "House").gender(MALE),
                Person.name("James", "Wilson").gender(MALE),
                Person.name("Lisa", "Cuddy").gender(FEMALE),
                Person.name("Allison", "Cameron").gender(FEMALE),
                Person.name("Eric", "Foreman").gender(MALE),
                Person.name("Robert", "Chase").gender(MALE),
                Person.name("Remy", "Hadley").gender(FEMALE),
                Person.name("Chris", "Taub").gender(MALE));
        assertThat(doctors, is(namedGroup()
                .withName(is("Doctors"))
                .withMen(containsInAnyOrder(
                        person().withFirstName(is("Gregory")),
                        person().withFirstName(is("James")),
                        person().withFirstName(is("Eric")),
                        person().withFirstName(is("Robert")),
                        person().withFirstName(is("Chris"))))
                .withWomen(containsInAnyOrder(
                        person().withFirstName(is("Lisa")),
                        person().withFirstName(is("Allison")),
                        person().withFirstName(is("Remy"))))));
    }
}
