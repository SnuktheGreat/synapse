package com.impressiveinteractive.synapse.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
public class ExceptionsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionsTest.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final FileNotFoundException cause = new FileNotFoundException("I couldn't find ~/Desktop/secrets");

    @Test
    public void format() throws Exception {
        IOException formatted = Exceptions.format(IOException::new,
                "Testing {}, {}, {}.", "one", "two", "three", cause);

        assertThat(formatted.getMessage(), is(equalTo("Testing one, two, three.")));
        assertThat(formatted.getCause(), is(cause));
    }

    @Test
    public void formatMessage() throws Exception {
        IOException formatted = Exceptions.formatMessage(IOException::new,
                "Testing {}, {}, {}.", "one", "two", "three");

        assertThat(formatted.getMessage(), is(equalTo("Testing one, two, three.")));
        assertThat(formatted.getCause(), is(nullValue()));
    }

    @Test
    public void formatMessage_exceptionArgumentAdded() throws Exception {
        exception.expect(IllegalArgumentException.class);
        Exceptions.formatMessage(IOException::new, "Testing {}, {}, {}.", "one", "two", "three", cause);
    }

    @Test
    public void wrapChecked() throws Exception {
        assertThat(Exceptions.wrapChecked(new NullPointerException()), is(instanceOf(NullPointerException.class)));

        assertThat(Exceptions.wrapChecked(new IOException()), is(instanceOf(RuntimeIOException.class)));
        assertThat(Exceptions.wrapChecked(new FileNotFoundException()), is(instanceOf(RuntimeIOException.class)));

        assertThat(Exceptions.wrapChecked(new InterruptedException()), is(instanceOf(RuntimeInterruptedException.class)));

        assertThat(Exceptions.wrapChecked(new Exception()), is(instanceOf(WrappedCheckedException.class)));
    }

    private void consume(String consumable) throws IOException {
        throw Exceptions.formatMessage(IOException::new, "I don't like this {}.", consumable);
    }

    @Test
    public void wrapConsumer() throws Exception {
        exception.expect(IOException.class);

        try {
            Stream.of("Apple", "Orange")
                    .forEach(Exceptions.wrap(this::consume));
        } catch (RuntimeIOException e) {
            e.unwrap(); // Throws original IOException
        }
    }

    private String supply() throws IOException {
        throw Exceptions.formatMessage(IOException::new, "My tummy hurts.");
    }

    @Test
    public void wrapSupplier() throws Exception {
        exception.expect(IOException.class);

        try {
            Stream.generate(Exceptions.wrap(this::supply))
                    .forEach(LOGGER::info);
        } catch (RuntimeIOException e) {
            e.unwrap(); // Throws original IOException
        }
    }

    private String transform(String consumable) throws IOException {
        throw Exceptions.formatMessage(IOException::new, "I don't like this {}.", consumable);
    }

    @Test
    public void wrapFunction() throws Exception {
        exception.expect(IOException.class);

        try {
            Stream.of("Apple", "Orange")
                    .map(Exceptions.wrap(this::transform))
                    .forEach(LOGGER::info);
        } catch (RuntimeIOException e) {
            e.unwrap(); // Throws original IOException
        }
    }
}