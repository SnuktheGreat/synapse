package com.impressiveinteractive.synapse.processor.test.people;

public class Person {

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

    public void dance(){
        // Use your imagination!
    }

    public enum Gender {
        MALE, FEMALE
    }
}