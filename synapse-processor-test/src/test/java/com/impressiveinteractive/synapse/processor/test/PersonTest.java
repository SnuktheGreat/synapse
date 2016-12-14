package com.impressiveinteractive.synapse.processor.test;

import com.impressiveinteractive.synapse.processor.BuildMatcher;
import com.impressiveinteractive.synapse.processor.test.people.PeopleUtilities;
import com.impressiveinteractive.synapse.processor.test.people.Person;
import org.junit.Test;

import static com.impressiveinteractive.synapse.processor.test.people.Person.Gender.MALE;
import static com.impressiveinteractive.synapse.processor.test.people.PersonMatcher.person;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@BuildMatcher(pojo = Person.class, utilities = PeopleUtilities.class)
public class PersonTest {

    @Test
    public void testGeneratedMatcher() throws Exception {
        assertThat(Person.name("James", "Wilson").gender(MALE).age(33).awesome(false),
                is(person()
                        .withFullName(equalTo("James Wilson"))
                        .withGender(is(MALE))
                        .withAge(is(33))
                        .withAwesome(is(false))));
    }
}
