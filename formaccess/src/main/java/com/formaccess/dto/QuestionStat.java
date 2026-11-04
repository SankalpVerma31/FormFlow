package com.formaccess.dto;

import java.util.List;

/** Statistics for one question. Only the fields that fit the question type are filled. */
public class QuestionStat {
    private Long questionId;
    private String questionText;
    private String type;
    private long responses;
    private Double average;
    private Double minimum;
    private Double maximum;
    private List<LabelCount> breakdown = List.of();   // options or YES/NO

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long v) { this.questionId = v; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String v) { this.questionText = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public long getResponses() { return responses; }
    public void setResponses(long v) { this.responses = v; }
    public Double getAverage() { return average; }
    public void setAverage(Double v) { this.average = v; }
    public Double getMinimum() { return minimum; }
    public void setMinimum(Double v) { this.minimum = v; }
    public Double getMaximum() { return maximum; }
    public void setMaximum(Double v) { this.maximum = v; }
    public List<LabelCount> getBreakdown() { return breakdown; }
    public void setBreakdown(List<LabelCount> v) { this.breakdown = v; }
}
