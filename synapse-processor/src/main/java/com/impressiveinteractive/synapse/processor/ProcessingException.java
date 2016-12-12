package com.impressiveinteractive.synapse.processor;

import javax.tools.Diagnostic;

public class ProcessingException extends Exception {
    private final Diagnostic.Kind kind;

    public ProcessingException(Diagnostic.Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Diagnostic.Kind getKind() {
        return kind;
    }
}
