package com.formaccess.entity.enums;

/** The 12 supported question types. Each knows how it is shown and stored. */
public enum QuestionType {
    SHORT_TEXT("Short text"),
    LONG_TEXT("Long text"),
    INTEGER("Integer number"),
    DECIMAL("Decimal number"),
    EMAIL("Email"),
    DATE("Date"),
    TIME("Time"),
    DROPDOWN("Dropdown (single choice)"),
    CHECKBOX("Checkboxes (multiple choice)"),
    RADIO("Radio buttons (single choice)"),
    RATING("Rating"),
    YES_NO("Yes / No");

    private final String label;

    QuestionType(String label) { this.label = label; }

    public String getLabel() { return label; }

    /** Types whose answers are chosen from a list of options. */
    public boolean isChoice() { return this == DROPDOWN || this == CHECKBOX || this == RADIO; }

    /** Types whose answers are numbers (we can compute average/min/max). */
    public boolean isNumeric() { return this == INTEGER || this == DECIMAL || this == RATING; }

    public boolean isText() { return this == SHORT_TEXT || this == LONG_TEXT || this == EMAIL; }
}
