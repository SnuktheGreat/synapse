package com.impressiveinteractive.synapse.reflect;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TypedTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testTypedFromClass() {
        Typed<String> stringTyped = Typed.of(String.class);

        assertThat(stringTyped.getType(), is(equalTo(String.class)));
        assertThat(stringTyped.getRawType(), is(equalTo(String.class)));
    }

    @Test
    public void testSimpleType() {
        Typed<String> stringTyped = new Typed<String>() {};

        assertThat(stringTyped.getType(), is(equalTo(String.class)));
        assertThat(stringTyped.getRawType(), is(equalTo(String.class)));
    }

    @Test
    public void testNestedType() {
        Typed<List<String>> stringTyped = new Typed<List<String>>() {};

        assertThat(stringTyped.getType().getTypeName(), is("java.util.List<java.lang.String>"));
        assertThat(stringTyped.getRawType(), is(equalTo(List.class)));
    }

    @Test
    public <T> void testUnboundType() {
        expectedException.expect(IllegalArgumentException.class);
        new Typed<T>() {};
    }
}