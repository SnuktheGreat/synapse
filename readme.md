# Synapse

Synapse is a library that provides some easy to use utilities for Java 8. It is made up out of two parts:

- [synapse-core](#synapse-core): Provides core utility classes;
- [synapse-test](#synapse-test): Provides testing utility classes and Hamcrest matchers.

## Quick Examples:

**Exception formatting**

```
IOException formatted = Exceptions.format(IOException::new,
    "Testing {}, {}, {}.", "one", "two", "three", cause);
```

See [SLF4J style exception message formatting](#slf4j-style-exception-message-formatting)

**Wrap checked exceptions**

```
Stream.of("Apple", "Orange")
        .forEach(Exceptions.wrapExceptional(references::consume, RuntimeIOException::new));
```

**Wrap and unwrap checked exceptions**

```
try {
    Stream.of("Apple", "Orange")
            .forEach(Exceptions.wrapExceptional(references::consume, WrappedIOException::new));
} catch (WrappedIOException e) {
    e.unwrap(); // Throws original IOException
}
```

**Lambda Tools**

```
String transform(Integer in);

...

assertThat(Lambdas.serializable(item::transform).getInputClass(), is(equalTo(Integer.class)));
assertThat(Lambdas.serializable(item::transform).getResultClass(), is(equalTo(String.class)));
```

**Chainable Hamcrest Matcher**

```
assertThat(person, is(ofType(Person.class)
        .where(Person::getFirstName, is("Steve"))
        .where(Person::getSurName, is("Jones"))
        .where(Person::getGender, is(Gender.MALE))
        .where(Person::getAge, is(43))
        .where(Person::isAwesome, is(true))));
```

## synapse-core

The core library contains very easy to use utility classes. It currently depends on slf4j-api only, making it very
lightweight. It is planned to remove this dependency also, since it's used for message formatting only.

### Exceptions

The `Exceptions` class can be used to simplify the creation and use of `Throwable` instances. It has the following
functionality:

- `Exceptions.format` and `Exceptions.formatMessage` - SLF4J style exception message formatting;
- `Exceptions.wrapExceptional` and derived - Allows wrapping (and unwrapping) checked exceptions in unchecked ones.

#### SLF4J style exception message formatting

Allows you to use SLF4J type message formatting to all exceptions that have the normal `Exception(String message, Throwable cause)` constructor. This turns ancient code like this:

```
throw new SomeException("Location " + oldLocation + " for consumer " + consumerId + " could not be updated to " + newLocation, cause);
```

Into a familiar and more readable:

```
throw Exceptions.format(SomeException::new, "Location {} for consumer {} could not be updated to {}.", oldLocation, consumerId, newLocation, cause);
```

The given cause in the example is completely optional as it is when you would log it using SLF4J.

It is also possible to disallow a cause altogether. In this case you can execute `Exceptions.formatMessage(...)`, which calls the `Exception(String messsage)` constructor instead. Note that adding a cause here will cause an IllegalArgumentException.

#### Wrap checked exceptions

Whether you like or dislike checked exceptions, the fact is that they do happen and occasionally they happen when you're making use of the Java 8 Stream API. The `Exceptions.wrapExceptional*` methods provide an easy way to wrap checked exceptions and leaves the choice of rethrowing the original to you.

Consider the following example where we want to find the lists for a given stream of class names:

```
Stream.of("java.util.List", "java.util.ArrayList", "java.util.LinkedList")
        .map(Class::forName) // throws checked ClassNotFoundException
        .filter(Class::isInterface)
        .collect(toList());
```

In this case the method we want to use (`Class.forName`), which is like a function but throws an exception. In Synergy this is called an _ExceptionalFunction_ and we can convert that to a normal _Function_ like so:
 
```
Stream.of("java.util.List", "java.util.ArrayList", "java.util.LinkedList")
        .map(Exceptions.wrapExceptionalFunction(Class::forName, RuntimeClassNotFoundException::new))
        .filter(Class::isInterface)
        .collect(toList());
```

Now when the `Class.forName(String)` method throws an exception it will be wrapped in a _RuntimeClassNotFoundException_. Many Runtime equivalents of checked types are available as part of Synapse, but you could also supply your own method reference to build a RuntimeException.

It is also possible wrap the exception temporarily and unwrap it once the stream completes. To do this you can use the same `Exceptions.wrapExceptional*` methods, but supply a `WrappedException` type instead. This will look like this:

```
try {
    Stream.of("java.util.List", "java.util.ArrayList", "java.util.LinkedList")
            .map(Exceptions.wrapExceptionalFunction(Class::forName, WrappedClassNotFoundException::new))
            .filter(Class::isInterface)
            .collect(toList());
} catch (WrappedClassNotFoundException e) {
    e.unwrap(); // Throws the original ClassNotFoundException.
}
```

Next to `wrapExceptionalFunction` for _Function_ lambdas, there also is a `wrapExceptionalConsumer`  for _Consumer_ lambdas and `wrapExceptionalSupplier` for _Supplier_ lambdas. There are also three `wrapExceptional` methods that infer the type from the given lambda automatically.