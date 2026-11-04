package com.formaccess.exception;

import com.formaccess.service.AccessResult;

/** Thrown when a submission is refused (not eligible, closed, duplicate ...). */
public class SubmissionBlockedException extends RuntimeException {
    private final AccessResult reason;

    public SubmissionBlockedException(AccessResult reason) {
        super(reason.getMessage());
        this.reason = reason;
    }

    public AccessResult getReason() { return reason; }
}
