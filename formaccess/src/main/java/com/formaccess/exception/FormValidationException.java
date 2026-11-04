package com.formaccess.exception;

import java.util.Map;

/** One or more answers in a submitted form were invalid. Key = question id, value = message. */
public class FormValidationException extends RuntimeException {
    private final Map<Long, String> errors;

    public FormValidationException(Map<Long, String> errors) {
        super("Please fix the highlighted answers.");
        this.errors = errors;
    }

    public Map<Long, String> getErrors() { return errors; }
}
