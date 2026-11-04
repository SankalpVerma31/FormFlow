package com.formaccess.entity.enums;

/** Comparison operators allowed in eligibility rules. */
public enum RuleOperator {
    EQUALS("="),
    NOT_EQUALS("≠"),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL("≥"),
    LESS_THAN("<"),
    LESS_OR_EQUAL("≤"),
    CONTAINS("contains"),
    IN("is one of");

    private final String symbol;
    RuleOperator(String symbol) { this.symbol = symbol; }
    public String getSymbol() { return symbol; }

    /** Operators that only make sense for numbers (YEAR). */
    public boolean isNumericOnly() {
        return this == GREATER_THAN || this == GREATER_OR_EQUAL || this == LESS_THAN || this == LESS_OR_EQUAL;
    }
}
