package com.formaccess.entity.enums;

/** Statistics a creator can choose to publish. */
public enum SummaryType {
    RESPONSE_COUNT("Total number of responses", false),
    SUBMISSION_PERCENTAGE("Submission percentage", false),
    BY_DEPARTMENT("Responses by department", false),
    AVERAGE("Average", true),
    MINIMUM("Minimum", true),
    MAXIMUM("Maximum", true),
    OPTION_COUNT("Count of each option", true),
    OPTION_PERCENTAGE("Percentage of each option", true),
    YES_NO_COUNT("YES / NO totals", true);

    private final String label;
    private final boolean perQuestion;

    SummaryType(String label, boolean perQuestion) { this.label = label; this.perQuestion = perQuestion; }

    public String getLabel() { return label; }
    public boolean isPerQuestion() { return perQuestion; }
}
