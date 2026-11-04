package com.formaccess.dto;

import com.formaccess.entity.Event;

/** Numbers shown on the creator dashboard for one event. */
public class EventProgress {
    private final Event event;
    private final long eligible;
    private final long submitted;

    public EventProgress(Event event, long eligible, long submitted) {
        this.event = event;
        this.eligible = eligible;
        this.submitted = submitted;
    }

    public Event getEvent() { return event; }
    public long getEligible() { return eligible; }
    public long getSubmitted() { return submitted; }
    public long getPending() { return Math.max(0, eligible - submitted); }
    public int getPercentage() {
        return eligible == 0 ? 0 : (int) Math.round(Math.min(100.0, submitted * 100.0 / eligible));
    }
}
