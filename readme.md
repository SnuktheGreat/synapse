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

**Lambda Tools**

```java
String transform(Integer in);

...

assertThat(Lambdas.serializable(item::transform).getInputClass(), is(equalTo(Integer.class)));
assertThat(Lambdas.serializable(item::transform).getResultClass(), is(equalTo(String.class)));
```

**Chainable Hamcrest Matcher**

```java
assertThat(person, is(ofType(Person.class)
        .where(Person::getFirstName, is("Steve"))
        .where(Person::getSurName, is("Jones"))
        .where(Person::getGender, is(Gender.MALE))
        .where(Person::getAge, is(43))
        .where(Person::isAwesome, is(true))));
```

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