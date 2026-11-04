package com.formaccess.dto;

import java.util.List;

/** Everything the analytics page and its charts need. Also returned as JSON by the REST API. */
public class EventStats {
    private final long eligible;
    private final long submitted;
    private final List<LabelCount> timeline;
    private final List<LabelCount> byDepartment;
    private final List<QuestionStat> questions;

    public EventStats(long eligible, long submitted, List<LabelCount> timeline,
                      List<LabelCount> byDepartment, List<QuestionStat> questions) {
        this.eligible = eligible;
        this.submitted = submitted;
        this.timeline = timeline;
        this.byDepartment = byDepartment;
        this.questions = questions;
    }

    public long getEligible() { return eligible; }
    public long getSubmitted() { return submitted; }
    public long getPending() { return Math.max(0, eligible - submitted); }
    public double getPercentage() {
        return eligible == 0 ? 0 : Math.round(Math.min(100.0, submitted * 100.0 / eligible) * 10) / 10.0;
    }
    public List<LabelCount> getTimeline() { return timeline; }
    public List<LabelCount> getByDepartment() { return byDepartment; }
    public List<QuestionStat> getQuestions() { return questions; }
}
