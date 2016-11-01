package com.impressiveinteractive.synapse.test;

import com.impressiveinteractive.synapse.lambda.SerializableFunction;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.impressiveinteractive.synapse.test.ChainableMatcher.map;
import static com.impressiveinteractive.synapse.test.ChainableMatcher.ofType;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class ChainableMatcherTest {

    // Demonstrations
    @Test
    public void testSimpleUseCase() throws Exception {
        assertThat(Person.name("Steve", "Jones").gender(Gender.MALE).age(43).awesome(true),
                is(ofType(Person.class)
                        .where(Person::getFirstName, is("Steve"))
                        .where(Person::getSurName, is("Jones"))
                        .where(Person::getGender, is(Gender.MALE))
                        .where(Person::getAge, is(43))
                        .where(Person::isAwesome, is(true))));
    }

    @Test
    public void testSimpleUseCase_failure() throws Exception {
        ChainableMatcher<Person> matcher = ofType(Person.class)
                .where(Person::getFirstName, is("Steve"))
                .where(Person::getSurName, is("Jones"))
                .where(Person::getGender, is(Gender.MALE))
                .where(Person::getAge, is(43))
                .where(Person::isAwesome, is(true));

        Person stella = Person.name("Stella", "Jones").gender(Gender.FEMALE).age(43).awesome(true);
        assertThat(matcher.matches(stella), is(false)); // Stella (Gender.FEMALE) instead of Steve (Gender.MALE)

        assertThat(describeMismatch(matcher, stella), allOf(
                containsString("firstName was \"Stella\""),
                containsString("expecting with firstName is \"Steve\""),
                containsString("gender was <FEMALE>"),
                containsString("expecting with gender is <MALE>")));
    }

    @Test
    public void testSimpleUseCase_extendedChainable() throws Exception {
        assertThat(
                Person.name("James", "Wilson").gender(Gender.MALE).age(33).awesome(false),
                isPerson()
                        .withFirstName(equalTo("James"))
                        .withSurName(equalTo("Wilson"))
                        .withGender(is(Gender.MALE))
                        .withAge(is(33))
                        .withAwesomeness(is(false)));
    }

    @Test
    public void testNestedUseCase() throws Exception {
        assertThat(
                couple()
                        .woman(Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(31).awesome(true))
                        .man(Person.name("James", "Wilson").gender(Gender.MALE).age(33).awesome(false)),
                ofType(Couple.class)
                        .where(Couple::getMan, is(ofType(Person.class)
                                .where(Person::getFirstName, is("James"))
                                .where(Person::getSurName, is("Wilson"))
                                .where(Person::getGender, is(Gender.MALE))
                                .where(Person::getAge, is(33))
                                .where(Person::isAwesome, is(false))))
                        .where(Couple::getWoman, is(ofType(Person.class)
                                .where(Person::getFirstName, is("Maria"))
                                .where(Person::getSurName, is("Wilson"))
                                .where(Person::getGender, is(Gender.FEMALE))
                                .where(Person::getAge, is(31))
                                .where(Person::isAwesome, is(true)))));
    }

    @Test
    public void testNestedUseCase_failure() throws Exception {
        ChainableMatcher<Couple> matcher = ofType(Couple.class)
                .where(Couple::getMan, is(ofType(Person.class)
                        .where(Person::getFirstName, is("James"))
                        .where(Person::getSurName, is("Wilson"))
                        .where(Person::getGender, is(Gender.MALE))
                        .where(Person::getAge, is(33))
                        .where(Person::isAwesome, is(false))))
                .where(Couple::getWoman, is(ofType(Person.class)
                        .where(Person::getFirstName, is("Maria"))
                        .where(Person::getSurName, is("Wilson"))
                        .where(Person::getGender, is(Gender.FEMALE))
                        .where(Person::getAge, is(31))
                        .where(Person::isAwesome, is(true))));

        Couple couple = couple()
                // Age 30 instead of expected 31
                .woman(Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(30).awesome(true))
                // First name Jimmy instead of expected James
                .man(Person.name("Jimmy", "Wilson").gender(Gender.MALE).age(33).awesome(false));
        assertThat(matcher.matches(couple), is(false));


        assertThat(describeMismatch(matcher, couple), allOf(
                containsString("man has unexpected value for:"),
                containsString("firstName was \"Jimmy\""),
                containsString("expecting with firstName is \"James\""),
                containsString("woman has unexpected value for:"),
                containsString("age was <30>"),
                containsString("expecting with age is <31>")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedUseCaseWithMapping() throws Exception {
        assertThat(
                people(
                        Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(31).awesome(true),
                        Person.name("James", "Wilson").gender(Gender.MALE).age(33).awesome(false)),
                is(ofType(People.class)
                        .where(map(People::getList)
                                        .to("get(0)", list -> list.get(0))
                                        .to(Person::getFirstName),
                                is("Maria"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedUseCaseWithMapping_failure() throws Exception {
        ChainableMatcher<People> matcher = ofType(People.class)
                .where(map(People::getList)
                                .to("get(0)", list -> list.get(0))
                                .to(Person::getFirstName),
                        is("James"));

        People people = people(
                Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(31).awesome(false));

        assertThat(matcher.matches(people), is(false));

        assertThat(describeMismatch(matcher, people), allOf(
                containsString("list.get(0).firstName was \"Maria\""),
                containsString("expecting with list.get(0).firstName is \"James\"")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedUseCaseWithMappingFromWhereAndMap_failure() throws Exception {
        ChainableMatcher<People> matcher = ofType(People.class)
                .where(map(this::firstPerson).to(Person::getFirstName), is("James"));

        People people = people(
                Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(31).awesome(false));

        assertThat(matcher.matches(people), is(false));

        assertThat(describeMismatch(matcher, people), allOf(
                containsString("firstPerson.firstName was \"Maria\""),
                containsString("expecting with firstPerson.firstName is \"James\"")));
    }

    private Person firstPerson(People people) {
        return people.getList().get(0);
    }

    @Test
    public void testWrongType() throws Exception {
        ChainableMatcher<Person> matcher = ofType(Person.class)
                .where(Person::getFirstName, is("Steve"))
                .where(Person::getSurName, is("Jones"))
                .where(Person::getGender, is(Gender.MALE))
                .where(Person::getAge, is(43));

        String aString = "This is a String.";
        assertThat(matcher.matches(aString), is(false)); // Obviously won't match Steve the Person.

        assertThat(describeMismatch(matcher, aString), is("of unexpected type java.lang.String"));
    }

    @Test
    public void testCustomLambdaName() throws Exception {
        Person maria = Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(31).awesome(true);
        Person james = Person.name("James", "Wilson").gender(Gender.MALE).age(33).awesome(false);
        Couple couple = couple().woman(maria).man(james);

        ChainableMatcher<Couple> matcher = ofType(Couple.class)
                .where("males", c -> c.get(Gender.MALE), contains(maria));

        assertThat(matcher.matches(couple), is(false)); // Maria is not a male

        assertThat(describeMismatch(matcher, couple), startsWith("has unexpected value for:\n\tmales item 0:"));
    }

    // Protected method
    @Test
    public void testDescribe() throws Exception {
        assertThat(ChainableMatcher.describe(Person::getAge), is("age"));
        assertThat(ChainableMatcher.describe(Person::isAwesome), is("awesome"));
        assertThat(ChainableMatcher.describe((SerializableFunction<List, Integer>) List::size), is("size()"));
    }

    @Test
    public void testDescribe_appendTo() throws Exception {
        String field = "field";

        assertThat(ChainableMatcher.describe(Person::getAge, field), is("field.age"));
        assertThat(ChainableMatcher.describe(Person::isAwesome, field), is("field.awesome"));
        assertThat(ChainableMatcher.describe((SerializableFunction<List, Integer>) List::size, field), is("field.size()"));

        Methods methods = new Methods();
        assertThat(ChainableMatcher.describe(methods::convert, field), is("Methods.convert(field)"));
        assertThat(ChainableMatcher.describe(Methods::convertStatic, field), is("Methods.convertStatic(field)"));

        assertThat(ChainableMatcher.describe(person -> "steve", field), is("<lambda>(field)"));
    }

    private String describeMismatch(ChainableMatcher<?> matcher, Object item) {
        StringDescription description = new StringDescription();
        matcher.describeMismatch(item, description);
        return description.toString();
    }

    private static Couple couple() {
        return new Couple();
    }

    private static PersonMatcher isPerson() {
        return new PersonMatcher();
    }

    private static People people(Person... persons) {
        return new People(Arrays.asList(persons));
    }

    private enum Gender {
        MALE, FEMALE
    }

    private static class Person {

        private final String firstName;
        private final String surName;
        private Gender gender;
        private int age;
        private boolean awesome;

        private Person(String firstName, String surName) {
            this.firstName = firstName;
            this.surName = surName;
        }

        public static Person name(String firstName, String surName) {
            return new Person(firstName, surName);
        }

        public String getFirstName() {
            return firstName;
        }

        public String getSurName() {
            return surName;
        }

        public Gender getGender() {
            return gender;
        }

        public Person gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public int getAge() {
            return age;
        }

        public Person age(int age) {
            this.age = age;
            return this;
        }

        public boolean isAwesome() {
            return awesome;
        }

        public Person awesome(boolean awesome) {
            this.awesome = awesome;
            return this;
        }
    }

    private static class Couple {

        private Person woman;
        private Person man;

        public Couple woman(Person woman) {
            this.woman = woman;
            return this;
        }

        public Couple man(Person man) {
            this.man = man;
            return this;
        }

        public Person getWoman() {
            return woman;
        }

        public Person getMan() {
            return man;
        }

        public List<Person> get(Gender gender) {
            return Stream.of(man, woman)
                    .filter(person -> person.gender == gender)
                    .collect(Collectors.toList());
        }
    }

    private static class People {

        private final List<Person> people;

        private People(List<Person> people) {
            this.people = people;
        }

        public List<Person> getList() {
            return people;
        }
    }

    private static class PersonMatcher extends ChainableMatcher<Person> {

        public PersonMatcher() {
            super(Person.class);
        }

        public PersonMatcher withFirstName(Matcher<String> matcher) {
            where(Person::getFirstName, matcher);
            return this;
        }

        public PersonMatcher withSurName(Matcher<String> matcher) {
            where(Person::getSurName, matcher);
            return this;
        }

        public PersonMatcher withGender(Matcher<Gender> matcher) {
            where(Person::getGender, matcher);
            return this;
        }

        public PersonMatcher withAge(Matcher<Integer> matcher) {
            where(Person::getAge, matcher);
            return this;
        }

        public PersonMatcher withAwesomeness(Matcher<Boolean> matcher) {
            where(Person::isAwesome, matcher);
            return this;
        }
    }
}