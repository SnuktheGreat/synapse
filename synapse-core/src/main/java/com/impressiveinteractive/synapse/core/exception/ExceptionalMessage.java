package com.impressiveinteractive.synapse.core.exception;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * A container for a message that may or may not come with a {@link Throwable}. Use {@link #parse(String, Object...)} to
 * create an ExceptionalMessage in the SLF4J format.
 */
public class ExceptionalMessage {
    private static final int ESCAPE = '\\';
    private static final int VAR_OPEN = '{';
    private static final int VAR_CLOSE = '}';

    private final String message;
    private final Throwable throwable;

    /**
     * Create an {@link ExceptionalMessage} from the given format string and args. This method has been created to work
     * exactly like log messages with SLF4J. Each occurrence of the string literal {@code {}} will be replaced in order
     * with {@link Object#toString()} from the args array. Example:
     *
     * <pre>
     * assertThat(ExceptionalMessage.parse("This is a {}", "test").getMessage(),
     *     is("This is a test"));
     * assertThat(ExceptionalMessage.parse("This is a {},{},{}", 1, 2, 3).getMessage(),
     *     is("This is a 1,2,3"));
     * </pre>
     *
     * The string literal {@code {}} can be escaped with a {@code \}. Example: {@code "\\{}"}. You can also escape the
     * backslash if you wish to have the literal {@code \}. Escaping has no effect in other situations.
     * <p>
     * When the final element of {@code args} is a {@link Throwable}, this exception will end up in
     * {@link ExceptionalMessage#getThrowable()}. This {@link Throwable} will not be used in message substitution.
     *
     * @param format The message format, where {@code {}} will bre replaced with elements from {@code args}.
     * @param args   Used to fill {@code {}} inside the format message.
     * @return An {@link ExceptionalMessage} with a message generated from {@code format} and {@code args} and
     * optionally a throwable, if this was the last element in the {@code args} array.
     */
    public static ExceptionalMessage parse(String format, Object... args) {
        StringBuilder messageBuilder = new StringBuilder();

        Object[] values;
        int valuesIndex = 0;
        Throwable throwable;
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            values = Arrays.copyOfRange(args, 0, args.length - 1);
            throwable = (Throwable) args[args.length - 1];
        } else {
            values = args;
            throwable = null;
        }

        boolean escaped = false;
        boolean open = false;

        int index = 0;
        while (index < format.length()) {
            int codePoint = format.codePointAt(index);
            if (open) {
                if (codePoint == VAR_CLOSE) {
                    if (escaped) {
                        messageBuilder.appendCodePoint(VAR_OPEN).appendCodePoint(VAR_CLOSE);
                        escaped = false;
                    } else if (valuesIndex < values.length) {
                        messageBuilder.append(values[valuesIndex++]);
                    } else {
                        messageBuilder.appendCodePoint(VAR_OPEN).appendCodePoint(VAR_CLOSE);
                    }
                } else {
                    messageBuilder.appendCodePoint(VAR_OPEN).appendCodePoint(codePoint);
                }
                open = false;
            } else if (codePoint == VAR_OPEN) {
                open = true;
            } else if (codePoint == ESCAPE) {
                if (escaped) {
                    messageBuilder.appendCodePoint(ESCAPE);
                } else {
                    escaped = true;
                }
            } else {
                if (escaped) {
                    messageBuilder.appendCodePoint(ESCAPE);
                    escaped = false;
                }
                messageBuilder.appendCodePoint(codePoint);
            }
            index += Character.charCount(codePoint);
        }
        return new ExceptionalMessage(messageBuilder.toString(), throwable);
    }

    /**
     * Create a new {@link ExceptionalMessage}.
     *
     * @param message   The string message, can not be null.
     * @param throwable The throwable, can be null.
     */
    public ExceptionalMessage(String message, Throwable throwable) {
        this.message = requireNonNull(message);
        this.throwable = throwable;
    }

    /**
     * @return The string message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The {@link Throwable}, or null.
     */
    public Throwable getThrowable() {
        return throwable;
    }
}
