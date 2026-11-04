package com.formaccess.service;

import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.DeadlineMode;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Moves events between SCHEDULED -> OPEN -> CLOSED.
 * Used in two places:
 *   1. the scheduler (every minute), and
 *   2. every time someone opens/submits a form (so we never depend only on the scheduler).
 */
@Service
public class DeadlineService {

    private static final Logger log = LoggerFactory.getLogger(DeadlineService.class);

    private final EventRepository eventRepository;
    private final AuditService auditService;

    public DeadlineService(EventRepository eventRepository, AuditService auditService) {
        this.eventRepository = eventRepository;
        this.auditService = auditService;
    }

    /** Bring one event's status up to date with the clock. */
    @Transactional
    public void refresh(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if (event.getStatus() == EventStatus.SCHEDULED && !now.isBefore(event.getStartAt())) {
            event.setStatus(EventStatus.OPEN);
            eventRepository.save(event);
            auditService.log(event, null, "AUTO_OPENED", "Start time reached, event opened");
            log.info("Event {} opened automatically", event.getId());
        }
        if (event.getStatus() == EventStatus.OPEN && event.isDeadlinePassed(now)) {
            close(event, null, "AUTO_CLOSED", "Deadline reached, closed automatically");
            log.info("Event {} closed automatically", event.getId());
        }
    }

    @Transactional
    public void close(Event event, User actor, String action, String details) {
        event.setStatus(EventStatus.CLOSED);
        event.setClosedAt(LocalDateTime.now());
        eventRepository.save(event);
        auditService.log(event, actor, action, details);
    }

    /** Called by EventStatusScheduler. */
    @Transactional
    public void processAll() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> toOpen = eventRepository.findByStatusAndStartAtLessThanEqual(EventStatus.SCHEDULED, now);
        List<Event> toClose = eventRepository.findByStatusAndDeadlineModeAndDeadlineAtLessThanEqual(
                EventStatus.OPEN, DeadlineMode.AUTOMATIC, now);
        toOpen.forEach(this::refresh);
        toClose.forEach(this::refresh);
    }
}
