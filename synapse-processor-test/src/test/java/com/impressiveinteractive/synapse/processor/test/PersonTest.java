package com.impressiveinteractive.synapse.processor.test;

import com.impressiveinteractive.synapse.generated.PersonMatcherThing;
import com.impressiveinteractive.synapse.processor.BuildMatcher;
import org.junit.Test;

import static com.impressiveinteractive.synapse.processor.test.Person.Gender.MALE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@BuildMatcher(
        pojo = Person.class,
        destinationPackage = "com.impressiveinteractive.synapse.generated",
        destinationName = "PersonMatcherThing",
        utilities = PeopleUtilities.class)
public class PersonTest {

    @Test
    public void testGeneratedMatcher() throws Exception {
        assertThat(Person.name("James", "Wilson").gender(MALE).age(33).awesome(false),
                is(PersonMatcherThing.person()
                        .withFullName(equalTo("James Wilson"))
                        .withGender(is(MALE))
                        .withAge(is(33))
                        .withAwesome(is(false))));
    }
}
