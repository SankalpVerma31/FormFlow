package com.formaccess.dto;

import com.formaccess.entity.Event;

/** One card on the participant dashboard. */
public class ParticipantEventView {
    private final Event event;
    private final boolean submitted;

    public ParticipantEventView(Event event, boolean submitted) {
        this.event = event;
        this.submitted = submitted;
    }

    public Event getEvent() { return event; }
    public boolean isSubmitted() { return submitted; }
}
