package com.impressiveinteractive.synapse.core.exception;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ExceptionalMessageTest {

    private static final RuntimeException EXCEPTION = new RuntimeException();

    @Test
    public void compareMessageWithSlf4j() {
        assertThat(ExceptionalMessage.parse("This is a test").getMessage(),
                is("This is a test"));
        assertThat(ExceptionalMessage.parse("This is a {}", "test").getMessage(),
                is("This is a test"));
        assertThat(ExceptionalMessage.parse("Testing {},{},{}", 1, 2, 3).getMessage(),
                is("Testing 1,2,3"));
        assertThat(ExceptionalMessage.parse("Testing {},{},{}", "test").getMessage(),
                is("Testing test,{},{}"));
        assertThat(ExceptionalMessage.parse("This is a {test}", "test").getMessage(),
                is("This is a {test}"));


        assertThat(ExceptionalMessage.parse("This is a \\{}", "test").getMessage(),
                is("This is a {}"));
        assertThat(ExceptionalMessage.parse("This is a \\ {}", "test").getMessage(),
                is("This is a \\ test"));
        assertThat(ExceptionalMessage.parse("This is a \\\\ {}", "test").getMessage(),
                is("This is a \\\\ test"));
        assertThat(ExceptionalMessage.parse("This is a {\\}", "test").getMessage(),
                is("This is a {\\}"));

        assertThat(ExceptionalMessage.parse("This is a {}", "test", EXCEPTION).getMessage(),
                is("This is a test"));
        assertThat(ExceptionalMessage.parse("This is a {} {}", "test", EXCEPTION).getMessage(),
                is("This is a test {}"));
        assertThat(ExceptionalMessage.parse("This is a test", "test", EXCEPTION).getMessage(),
                is("This is a test"));
        assertThat(ExceptionalMessage.parse("This is a {} {}", EXCEPTION, "test").getMessage(),
                is("This is a java.lang.RuntimeException test"));
    }

    @Test
    public void compareThrowableWithSlf4j() {
        assertThat(ExceptionalMessage.parse("This is a test").getThrowable(),
                is(nullValue()));
        assertThat(ExceptionalMessage.parse("This is a {}", "test").getThrowable(),
                is(nullValue()));
        assertThat(ExceptionalMessage.parse("This is a {}", "test", EXCEPTION).getThrowable(),
                is(EXCEPTION));
        assertThat(ExceptionalMessage.parse("This is a {} {}", "test", EXCEPTION).getThrowable(),
                is(EXCEPTION));
        assertThat(ExceptionalMessage.parse("This is a test", "test", EXCEPTION).getThrowable(),
                is(EXCEPTION));
        assertThat(ExceptionalMessage.parse("This is a {} {}", EXCEPTION, "test").getThrowable(),
                is(nullValue()));
    }
}