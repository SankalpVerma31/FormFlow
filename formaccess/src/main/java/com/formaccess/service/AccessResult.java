package com.formaccess.service;

/** The outcome of "may this user fill this form right now?" */
public enum AccessResult {
    ALLOWED("You can fill this form."),
    NOT_PUBLISHED("This form does not exist or has not been published."),
    ACCOUNT_INACTIVE("Your account is deactivated. Contact the administrator."),
    NOT_ELIGIBLE("You are not eligible to participate in this event."),
    NOT_STARTED("This form is not open yet."),
    CLOSED("The deadline has passed. This form is no longer accepting responses."),
    ALREADY_SUBMITTED("You have already submitted this form.");

    private final String message;
    AccessResult(String message) { this.message = message; }
    public String getMessage() { return message; }
}
