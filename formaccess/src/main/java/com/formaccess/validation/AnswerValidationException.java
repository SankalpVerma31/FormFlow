package com.formaccess.validation;

/** Thrown when one answer breaks its question's rules. The message is shown to the user. */
public class AnswerValidationException extends RuntimeException {
    public AnswerValidationException(String message) { super(message); }
}
