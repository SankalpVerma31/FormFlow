package com.formaccess.service;

import com.formaccess.dto.EventProgress;
import com.formaccess.dto.ParticipantEventView;
import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.AuditLog;
import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.entity.enums.Role;
import com.formaccess.repository.AuditLogRepository;
import com.formaccess.repository.EventRepository;
import com.formaccess.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Collects the numbers and lists shown on the three dashboards. */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AuditLogRepository auditLogRepository;
    private final EventService eventService;
    private final EligibilityService eligibilityService;
    private final SubmissionService submissionService;
    private final DeadlineService deadlineService;

    public DashboardService(UserRepository userRepository, EventRepository eventRepository,
                            AuditLogRepository auditLogRepository, EventService eventService,
                            EligibilityService eligibilityService, SubmissionService submissionService,
                            DeadlineService deadlineService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventService = eventService;
        this.eligibilityService = eligibilityService;
        this.submissionService = submissionService;
        this.deadlineService = deadlineService;
    }

    public Map<String, Long> adminCounts() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("users", userRepository.count());
        m.put("admins", userRepository.countByRole(Role.ADMIN));
        m.put("creators", userRepository.countByRole(Role.CREATOR));
        m.put("participants", userRepository.countByRole(Role.PARTICIPANT));
        m.put("events", eventRepository.count());
        m.put("open", eventRepository.countByStatus(EventStatus.OPEN));
        m.put("closed", eventRepository.countByStatus(EventStatus.CLOSED));
        m.put("scheduled", eventRepository.countByStatus(EventStatus.SCHEDULED));
        m.put("draft", eventRepository.countByStatus(EventStatus.DRAFT));
        return m;
    }

    public List<AuditLog> recentActivity() {
        return auditLogRepository.findTop15ByOrderByCreatedAtDesc();
    }

    @Transactional
    public List<EventProgress> creatorEvents(User creator) {
        List<EventProgress> list = new ArrayList<>();
        for (Event e : eventService.eventsOf(creator)) {
            deadlineService.refresh(e);
            list.add(eventService.progress(e));
        }
        return list;
    }

    /** Participant dashboard: four lists. */
    public static class ParticipantDashboard {
        private final List<ParticipantEventView> pending = new ArrayList<>();
        private final List<ParticipantEventView> upcoming = new ArrayList<>();
        private final List<ParticipantEventView> submitted = new ArrayList<>();
        private final List<ParticipantEventView> closed = new ArrayList<>();

        public List<ParticipantEventView> getPending() { return pending; }
        public List<ParticipantEventView> getUpcoming() { return upcoming; }
        public List<ParticipantEventView> getSubmitted() { return submitted; }
        public List<ParticipantEventView> getClosed() { return closed; }
    }

    @Transactional
    public ParticipantDashboard participantDashboard(User user) {
        ParticipantDashboard d = new ParticipantDashboard();
        List<Event> events = eventRepository.findByStatusInOrderByDeadlineAtAsc(
                List.of(EventStatus.SCHEDULED, EventStatus.OPEN, EventStatus.CLOSED));
        for (Event e : events) {
            deadlineService.refresh(e);
            if (!eligibilityService.isEligible(user, e)) continue;      // ineligible events are not even listed
            boolean done = submissionService.hasSubmitted(user, e);
            ParticipantEventView v = new ParticipantEventView(e, done);
            if (done) d.submitted.add(v);
            if (e.getStatus() == EventStatus.SCHEDULED) d.upcoming.add(v);
            else if (e.getStatus() == EventStatus.OPEN && (!done || e.isAllowMultipleSubmissions())) d.pending.add(v);
            else if (e.getStatus() == EventStatus.CLOSED) d.closed.add(v);
        }
        return d;
    }
}
