package com.impressiveinteractive.synapse.exception;

import com.impressiveinteractive.synapse.exception.runtime.RuntimeIOException;
import com.impressiveinteractive.synapse.exception.wrapped.WrappedIOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored", "EndlessStream"})
public class ExceptionsTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private MethodReferences references;

    private final FileNotFoundException cause = new FileNotFoundException("I couldn't find ~/Desktop/secrets");

    @Test
    public void format() {
        IOException formatted = Exceptions.format(IOException::new,
                "Testing {}, {}, {}.", "one", "two", "three", cause);

        assertThat(formatted.getMessage(), is(equalTo("Testing one, two, three.")));
        assertThat(formatted.getCause(), is(cause));
    }

    @Test
    public void formatMessage() {
        IOException formatted = Exceptions.formatMessage(IOException::new,
                "Testing {}, {}, {}.", "one", "two", "three");

        assertThat(formatted.getMessage(), is(equalTo("Testing one, two, three.")));
        assertThat(formatted.getCause(), is(nullValue()));
    }

    @Test
    public void formatMessage_exceptionArgumentAdded() {
        exception.expect(IllegalArgumentException.class);
        Exceptions.formatMessage(IOException::new, "Testing {}, {}, {}.", "one", "two", "three", cause);
    }

    @Test
    public void wrapExceptional_exampleRuntimeEquivalent() {
        List<String> types = Stream.of("/does/not/exist/text.txt", "/does/not/exist/image.jpg")
                .map(File::new)
                .map(File::toPath)
                .map(Exceptions.wrapExceptional(Files::probeContentType, RuntimeIOException::new))
                .collect(toList());

        assertThat(types, contains("text/plain", "image/jpeg"));
    }

    @Test
    public void wrapExceptional_exampleWrappedEquivalent() throws Exception {
        try {
            List<String> types = Stream.of("/does/not/exist/text.txt", "/does/not/exist/image.jpg")
                    .map(File::new)
                    .map(File::toPath)
                    .map(Exceptions.wrapExceptional(Files::probeContentType, WrappedIOException::new))
                    .collect(toList());
            assertThat(types, contains("text/plain", "image/jpeg"));
        } catch (WrappedIOException e) {
            e.unwrap();
        }
    }

    @Test
    public void wrapConsumer() throws Exception {
        doThrow(new IOException()).when(references).consume(anyString());

        exception.expect(RuntimeIOException.class);

        Stream.of("Apple", "Orange")
                .forEach(Exceptions.wrapExceptional(references::consume, RuntimeIOException::new));
    }

    @Test
    public void wrapConsumer_andUnwrap() throws Exception {
        IOException expected = new IOException();
        doThrow(expected).when(references).consume(anyString());

        exception.expect(is(expected));

        try {
            Stream.of("Apple", "Orange")
                    .forEach(Exceptions.wrapExceptional(references::consume, WrappedIOException::new));
        } catch (WrappedIOException e) {
            e.unwrap(); // Throws original IOException
        }
    }

    @Test
    public void wrapConsumer_RuntimeException() throws Exception {
        RuntimeException expected = new RuntimeException();
        doThrow(expected).when(references).consume(anyString());

        exception.expect(is(expected));

        Stream.of("Apple", "Orange")
                .forEach(Exceptions.wrapExceptional(references::consume, RuntimeIOException::new));
    }

    @Test
    public void wrapSupplier() throws Exception {
        when(references.supply()).thenThrow(new IOException());

        exception.expect(RuntimeIOException.class);

        Stream.generate(Exceptions.wrapExceptional(references::supply, RuntimeIOException::new))
                .forEach(this::consume);
    }

    @Test
    public void wrapSupplier_andUnwrap() throws Exception {
        IOException expected = new IOException();
        when(references.supply()).thenThrow(expected);

        exception.expect(is(expected));

        try {
            Stream.generate(Exceptions.wrapExceptional(references::supply, WrappedIOException::new))
                    .forEach(this::consume);
        } catch (WrappedIOException e) {
            e.unwrap(); // Throws original IOException
        }
    }

    @Test
    public void wrapSupplier_RuntimeException() throws Exception {
        RuntimeException expected = new RuntimeException();
        when(references.supply()).thenThrow(expected);

        exception.expect(is(expected));

        Stream.generate(Exceptions.wrapExceptional(references::supply, RuntimeIOException::new))
                .forEach(this::consume);
    }

    @Test
    public void wrapFunction() throws Exception {
        when(references.transform(anyString())).thenThrow(new IOException());

        exception.expect(RuntimeIOException.class);

        Stream.of("Apple", "Orange")
                .map(Exceptions.wrapExceptional(references::transform, RuntimeIOException::new))
                .forEach(this::consume);
    }

    @Test
    public void wrapFunction_andUnwrap() throws Exception {
        IOException expected = new IOException();
        when(references.transform(anyString())).thenThrow(expected);

        exception.expect(is(expected));

        try {
            Stream.of("Apple", "Orange")
                    .map(Exceptions.wrapExceptional(references::transform, WrappedIOException::new))
                    .forEach(this::consume);
        } catch (WrappedIOException e) {
            e.unwrap(); // Throws original IOException
        }
    }

    @Test
    public void wrapFunction_RuntimeException() throws Exception {
        RuntimeException expected = new RuntimeException();
        when(references.transform(anyString())).thenThrow(expected);

        exception.expect(is(expected));

        Stream.of("Apple", "Orange")
                .map(Exceptions.wrapExceptional(references::transform, RuntimeIOException::new))
                .forEach(this::consume);
    }

    @SuppressWarnings("unused")
    private void consume(String string) {
        // Noop, used to trigger terminal stream operations.
    }

    private interface MethodReferences {
        void consume(String consumable) throws IOException;

        String supply() throws IOException;

        String transform(String consumable) throws IOException;
    }
}