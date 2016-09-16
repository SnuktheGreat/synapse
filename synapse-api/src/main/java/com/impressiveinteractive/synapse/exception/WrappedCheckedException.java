package com.impressiveinteractive.synapse.exception;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import static java.util.Objects.requireNonNull;

public class WrappedCheckedException extends RuntimeException {

    public WrappedCheckedException(Exception e) {
        super(e);
    }

    public WrappedCheckedException(Exception e, String message, Object... args) {
        this(MessageFormatter.arrayFormat(message, args, requireNonNull(e)));
    }

    private WrappedCheckedException(FormattingTuple tuple) {
        super(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public synchronized Exception getCause() {
        return (Exception) super.getCause();
    }

    public void unwrap() throws Exception {
        throw getCause();
    }
}
