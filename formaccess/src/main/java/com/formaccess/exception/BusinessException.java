package com.formaccess.exception;

/**
 * A rule of the application was broken, e.g. "cannot delete an event that has submissions".
 * GlobalExceptionHandler shows the message as a red alert on the previous page.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
