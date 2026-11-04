package com.formaccess.dto;

/** A label with a count and percentage, e.g. ("Just right", 3, 60.0). Also sent as JSON to charts. */
public class LabelCount {
    private final String label;
    private final long count;
    private final double percentage;

    public LabelCount(String label, long count, double percentage) {
        this.label = label;
        this.count = count;
        this.percentage = percentage;
    }

    public String getLabel() { return label; }
    public long getCount() { return count; }
    public double getPercentage() { return percentage; }
}
