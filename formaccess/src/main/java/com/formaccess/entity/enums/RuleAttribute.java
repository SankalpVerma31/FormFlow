package com.formaccess.entity.enums;

/** User attributes a creator may use in eligibility rules (a whitelist). */
public enum RuleAttribute {
    DEPARTMENT("Department"),
    YEAR("Year of study"),
    ROLE("Role"),
    ACCOUNT_STATUS("Account status");

    private final String label;
    RuleAttribute(String label) { this.label = label; }
    public String getLabel() { return label; }
}
