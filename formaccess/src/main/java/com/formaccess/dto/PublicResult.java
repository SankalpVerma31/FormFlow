package com.formaccess.dto;

import java.util.List;

/** One published statistic as participants see it – aggregated numbers only. */
public class PublicResult {
    private final String title;
    private final String value;
    private final List<LabelCount> breakdown;

    public PublicResult(String title, String value, List<LabelCount> breakdown) {
        this.title = title;
        this.value = value;
        this.breakdown = breakdown;
    }

    public String getTitle() { return title; }
    public String getValue() { return value; }
    public List<LabelCount> getBreakdown() { return breakdown; }
}
