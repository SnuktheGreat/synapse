# Synapse

Synapse is a library that provides some easy to use utilities for Java 8. It is made up out of two parts:

- [synapse-core](#synapse-core): Provides core utility classes;
- [synapse-test](#synapse-test): Provides testing utility classes and Hamcrest matchers.

## Quick Examples:

**Exception formatting**

```java
IOException formatted = Exceptions.format(IOException::new,
    "Testing {}, {}, {}.", "one", "two", "three", cause);
```

See [SLF4J style exception message formatting](#slf4j-style-exception-message-formatting)

**Wrap checked exceptions**

```java
Stream.of("Apple", "Orange")
        .forEach(Exceptions.wrapExceptional(references::consume, RuntimeIOException::new));
```

You can also choose to wrap and unwrap checked exceptions.

```java
try {
    Stream.of("Apple", "Orange")
            .forEach(Exceptions.wrapExceptional(references::consume, WrappedIOException::new));
} catch (WrappedIOException e) {
    e.unwrap(); // Throws original IOException
}
```

See [Wrap checked exceptions](#wrap-checked-exceptions)

**Lambdas**

```java
String transform(Integer in);

...

assertThat(Lambdas.serializable(item::transform).getInputClass(), is(equalTo(Integer.class)));
assertThat(Lambdas.serializable(item::transform).getResultClass(), is(equalTo(String.class)));
```

See [Lambdas](#lambdas)

**Chainable Hamcrest Matcher**

```java
assertThat(person, is(ofType(Person.class)
        .where(Person::getFirstName, is("Steve"))
        .where(Person::getSurName, is("Jones"))
        .where(Person::getGender, is(Gender.MALE))
        .where(Person::getAge, is(43))
        .where(Person::isAwesome, is(true))));
```

See [_ChainableMatcher_](#chainablematcher)

## synapse-core

The core library contains very easy to use utility classes. It currently depends on _slf4j-api_ only, making it very
lightweight. It is planned to remove this dependency also, since it's used for message formatting only.

### Exceptions

The _Exceptions_ class can be used to simplify the creation and use of _Throwable_ instances. It has the following
functionality:

- `Exceptions.format` and `Exceptions.formatMessage` - SLF4J style exception message formatting;
- `Exceptions.wrapExceptional` and derived - Allows wrapping (and unwrapping) checked exceptions in unchecked ones.

#### SLF4J style exception message formatting

Allows you to use SLF4J type message formatting to all exceptions that have the normal `Exception(String message,
Throwable cause)` constructor. This turns ancient code like this:

```java
throw new SomeException("Location " + oldLocation + " for consumer " + consumerId + " could not be updated to "
        + newLocation, cause);
```

Into a familiar and more readable:

```java
throw Exceptions.format(SomeException::new, "Location {} for consumer {} could not be updated to {}.",
        oldLocation, consumerId, newLocation, cause);
```

The given cause in the example is optional as it is when you would log it using SLF4J.

It is also possible to disallow a cause altogether. In this case you can execute `Exceptions.formatMessage(...)`, which
calls the `Exception(String messsage)` constructor instead. Note that adding a cause here will cause an
_IllegalArgumentException_.

#### Wrap checked exceptions

Whether you like or dislike checked exceptions, the fact is that they do happen and occasionally they happen when you're
making use of the Java 8 Stream API. The `Exceptions.wrapExceptional` methods provide an easy way to wrap checked
exceptions and leaves the choice of rethrowing the original to you.

Consider the following example where we want to find the MIME type for a given stream of file names:

```java
Stream.of("/does/not/exist/text.txt", "/does/not/exist/image.jpg")
        .map(File::new)
        .map(File::toPath)
        .map(Files::probeContentType) // Throws IOException
        .forEach(mimeType -> LOGGER.info("Type is {}.", mimeType));
```

The problem above is that method `Files.probeContentType` throws an IOException and a regular _Function_ does not allow
this. In Synergy an exception throwing function is called an _ExceptionalFunction_ and we can convert that to a normal
_Function_ like so:
 
```java
Stream.of("/does/not/exist/text.txt", "/does/not/exist/image.jpg")
        .map(File::new)
        .map(File::toPath)
        .map(Exceptions.wrapExceptional(Files::probeContentType, RuntimeIOException::new))
        .forEach(mimeType -> LOGGER.info("Type is {}.", mimeType));
```

Now when the `Files.probeContentType` method throws an exception it will be wrapped in a _RuntimeIOException_. You can
still catch this exception, but since it's a _RuntimeException_ it is allowed in the `Stream.map` method.

Many _Runtime_ equivalents of checked types are available as part of Synapse (see the
`com.impressiveinteractive.synapse.exception.runtime` package), but you could also supply your own method reference
that creates your own _RuntimeException_.

It is also possible wrap the exception temporarily and unwrap it once the stream completes. To do this you can use the
same `Exceptions.wrapExceptional` methods, but supply a `WrappedException` type instead. This will look like this:

```java
try {
    Stream.of("/does/not/exist/text.txt", "/does/not/exist/image.jpg")
            .map(File::new)
            .map(File::toPath)
            .map(Exceptions.wrapExceptional(Files::probeContentType, WrappedIOException::new))
            .forEach(mimeType -> LOGGER.info("Type is {}.", mimeType));
} catch (WrappedIOException e) {
    e.unwrap();
}
```

In the above example the _WrappedIOException_ is caught and the `unwrap()` methods throws the original _IOException_.
You can also throw it yourself by using `WrappedIOException.getCause()`, which returns the case as an _IOException_ 
directly.

Many _Wrapped_ equivalents of checked types are also available as part of Synapse (see the
`com.impressiveinteractive.synapse.exception.wrapped` package), but you could also supply your own method reference
that creates your own _WrappedException_.

The `Exceptions.wrapExceptional` method works on _Consumer_, _Function_ and _Supplier_ type lambdas. If the type of
lambda can not be determined automatically, you can use the more specific `Exceptions.wrapExceptionalConsumer`,
`Exceptions.wrapExceptionalFunction` and `Exceptions.wrapExceptionalSupplier`.

### Lambdas

The _Lambdas_ class is built around the _SerializableLambda_, which contains a `default SerializedLambda serialized()`
method. The [_SerializedLambda_](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/SerializedLambda.html)
returned by this method can be used to get extra information about the lambda. It for instance contains where it was
given, what kind of lambda it is, what method it refers to etc. In Synapse this information is used for the
[_ChainableMatcher_](#chainablematcher) for instance.

Synapse comes with three _SerializableLambda_ types: The _SerializableConsumer_, the _SerializableFunction_ and the
_SerializableSupplier_. All these three extend their original functional interface (_Consumer_, _Function_ and
_Supplier_ respectively) and the _SerializableLambda_ class. When accepting one of these types in any of your methods,
java will automatically convert any regular lambda to the serializable type. Example:

```java
public <T, R> void describe(SerializableFunction<T, R> function){
    LOGGER.info("Function converts a {} to {}.", function.getInputClass(), function.getResultClass());
}

...

describe((Integer i) -> ""+i); // Logs: Function converts a class java.lang.Integer to class java.lang.String.
```

To get access to a _SerializableLambda_ right away, you can use the `Lambdas.serializable(...)` methods, which takes
either of the three subtypes as an argument and returns it. You can also use the more specific
`Lambdas.serializableConsumer`, `Lambdas.serializableFunction` and `Lambdas.serializableSupplier` methods.

For now there are three other methods on the _Lambdas_ class, which all accept a _SerializedLambda_ as an argument.
These are:

- `Lambdas.getRawReturnType(SerializedLambda lambda)` - Get the raw return type for the given _SerializedLambda_;
- `Lambdas.getRawParameterTypes(SerializedLambda lambda)` - Get the raw type for all parameters on the given
  _SerializedLambda_;
- `Lambdas.getRawParameterType(SerializedLambda lambda, int index)` - Get the raw parameter type for the parameter at
  index _i_ on the given _SerializedLambda_.

The `getInputClass()` method on _SerializableConsumer_ and _SerializableFunction_ and `getResultClass()` on
_SerializableFunction_ and _SerializableSupplier_ are default convenience methods to one of these methods above.

More methods may be add in the future.

## synapse-test

Synapse test is the test module of the Synapse library. For now it gives access to one new type of _Hamcrest Matcher_,
the ChainableMatcher.

### ChainableMatcher

The _ChainableMatcher_ allows you to write test code for any type of custom object you want to test, without having to
build a custom _Matcher_. Consider for instance having a _Person_ instance and you need to test whether the name, age,
gender etc. are all correct. You can do this in multiple lines like: `assertThat(person.getFirstName(), is("David"))`,
but ideally you would create a custom _PersonMatcher_. This however requires quite a lot of plumbing.

With the _ChainableMatcher_ the above example with the Person would look like this:

```java
assertThat(Person.name("Stella", "Jones").gender(Gender.FEMALE).age(43).awesome(true),
        is(ofType(Person.class)
                .where(Person::getFirstName, is("Steve"))
                .where(Person::getSurName, is("Jones"))
                .where(Person::getGender, is(Gender.MALE))
                .where(Person::getAge, is(43))
                .where(Person::isAwesome, is(true))));
```

The _ChainableMatcher_ uses the given lambda/matcher combinations to check whether all these combinations are true on
the given person. The keen observer will have noticed that in the above case the matcher fails. When the
_ChainableMatcher_ fails, it will point you directly to the `where(...)` statements that do not match. For the example
above it reports:

```java
java.lang.AssertionError: 
Expected: is of type Person with firstName is "Steve" with surName is "Jones" with gender is <MALE> with age is <43> with awesome is <true>
     but: has unexpected value for:
	firstName was "Stella"
		expecting with firstName is "Steve"
	gender was <FEMALE>
		expecting with gender is <MALE>
```

The _ChainableMatcher_ uses the given lambdas to describe which fields you want to test and the matchers are used to
describe which value is expected.

#### Custom descriptions

TODO: 

#### Custom Matcher Class

One could still argue that the above example is a little verbose and that they'd still rather have a custom _Matcher_.
A custom matcher would look something like this:

```java
assertThat(
        Person.name("James", "Wilson").gender(Gender.MALE).age(33).awesome(false),
        isPerson()
                .withFirstName(equalTo("James"))
                .withSurName(equalTo("Wilson"))
                .withGender(is(Gender.MALE))
                .withAge(is(33))
                .withAwesomeness(is(false)))
```

The above example however uses the _ChainableMatcher_ as a base, so you still get the same functionality. The _Matcher_
class looks like this:

```java
public class PersonMatcher extends ChainableMatcher<Person> {
    
    public static PersonMatcher isPerson() {
        return new PersonMatcher();
    }

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
```

#### Nested Objects

TODO

```java
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
```

#### Mapping values

TODO:

```java
assertThat(
        people(
                Person.name("Maria", "Wilson").gender(Gender.FEMALE).age(31).awesome(true),
                Person.name("James", "Wilson").gender(Gender.MALE).age(33).awesome(false)),
        is(ofType(People.class)
                .where(map(People::getList)
                                .to("get(0)", list -> list.get(0))
                                .to(Person::getFirstName),
                        is("Maria"))));
```