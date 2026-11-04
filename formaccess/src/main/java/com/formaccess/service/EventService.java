package com.formaccess.service;

import com.formaccess.dto.EventForm;
import com.formaccess.dto.EventProgress;
import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.entity.enums.Role;
import com.formaccess.entity.enums.SubmissionStatus;
import com.formaccess.exception.BusinessException;
import com.formaccess.exception.NotFoundException;
import com.formaccess.repository.EventRepository;
import com.formaccess.repository.SubmissionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Creating, editing, publishing, closing and deleting events. */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SubmissionRepository submissionRepository;
    private final EligibilityService eligibilityService;
    private final DeadlineService deadlineService;
    private final AuditService auditService;

    public EventService(EventRepository eventRepository, SubmissionRepository submissionRepository,
                        EligibilityService eligibilityService, DeadlineService deadlineService,
                        AuditService auditService) {
        this.eventRepository = eventRepository;
        this.submissionRepository = submissionRepository;
        this.eligibilityService = eligibilityService;
        this.deadlineService = deadlineService;
        this.auditService = auditService;
    }

    /**
     * Loads an event that the current user is allowed to MANAGE (its creator or an admin).
     * Every creator page goes through this method -> ownership is always checked on the server.
     */
    public Event getManagedEvent(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        boolean owner = event.getCreator().getId().equals(user.getId());
        if (!owner && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can only manage your own events.");
        }
        deadlineService.refresh(event);
        return event;
    }

    public Event getByToken(String token) {
        return eventRepository.findByPublicToken(token).orElseThrow(() -> new NotFoundException("Form not found"));
    }

    public List<Event> eventsOf(User creator) {
        return eventRepository.findByCreatorOrderByCreatedAtDesc(creator);
    }

    public List<Event> allEvents() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public EventProgress progress(Event event) {
        return new EventProgress(event, eligibilityService.countEligible(event),
                submissionRepository.countByEventAndStatus(event, SubmissionStatus.SUBMITTED));
    }

    @Transactional
    public Event create(EventForm form, User creator) {
        validateDates(form, true);
        Event event = new Event();
        event.setPublicToken(UUID.randomUUID().toString());
        event.setCreator(creator);
        event.setStatus(EventStatus.DRAFT);
        apply(event, form);
        eventRepository.save(event);
        auditService.log(event, creator, "CREATED", "Event created");
        return event;
    }

    @Transactional
    public void update(Event event, EventForm form, User actor) {
        if (event.getStatus() == EventStatus.CLOSED) throw new BusinessException("A closed event cannot be edited.");
        validateDates(form, true);
        apply(event, form);
        eventRepository.save(event);
        auditService.log(event, actor, "UPDATED", "Event details updated");
        deadlineService.refresh(event);
    }

    @Transactional
    public void publish(Event event, User actor) {
        if (event.getStatus() != EventStatus.DRAFT) throw new BusinessException("Only a draft can be published.");
        if (event.getQuestions().isEmpty()) throw new BusinessException("Add at least one question before publishing.");
        LocalDateTime now = LocalDateTime.now();
        if (!event.getDeadlineAt().isAfter(now)) throw new BusinessException("The deadline is in the past. Edit the event first.");
        event.setPublishedAt(now);
        event.setStatus(event.getStartAt().isAfter(now) ? EventStatus.SCHEDULED : EventStatus.OPEN);
        eventRepository.save(event);
        auditService.log(event, actor, "PUBLISHED", "Published with status " + event.getStatus());
    }

    @Transactional
    public void closeManually(Event event, User actor) {
        if (event.getStatus() == EventStatus.CLOSED) throw new BusinessException("The event is already closed.");
        if (event.getStatus() == EventStatus.DRAFT) throw new BusinessException("A draft cannot be closed. Delete it instead.");
        deadlineService.close(event, actor, "MANUALLY_CLOSED", "Closed by " + actor.getFullName());
    }

    @Transactional
    public void delete(Event event, User actor) {
        if (submissionRepository.countByEvent(event) > 0) {
            throw new BusinessException("This event already has submissions, so it cannot be deleted. Close it instead.");
        }
        String details = "Deleted event '" + event.getTitle() + "' (#" + event.getId() + ")";
        eventRepository.delete(event);
        auditService.log(null, actor, "DELETED", details);
    }

    public EventForm toForm(Event event) {
        EventForm form = new EventForm();
        form.setTitle(event.getTitle());
        form.setDescription(event.getDescription());
        form.setStartAt(event.getStartAt());
        form.setDeadlineAt(event.getDeadlineAt());
        form.setDeadlineMode(event.getDeadlineMode());
        form.setAllowMultipleSubmissions(event.isAllowMultipleSubmissions());
        form.setAutoReminderEnabled(event.isAutoReminderEnabled());
        form.setReminderHoursBefore(event.getReminderHoursBefore() == null ? 24 : event.getReminderHoursBefore());
        return form;
    }

    private void validateDates(EventForm form, boolean deadlineMustBeFuture) {
        if (!form.getDeadlineAt().isAfter(form.getStartAt())) {
            throw new BusinessException("The deadline must be after the start date.");
        }
        if (deadlineMustBeFuture && !form.getDeadlineAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("The deadline must be in the future.");
        }
        if (form.isAutoReminderEnabled() && form.getReminderHoursBefore() == null) {
            throw new BusinessException("Enter how many hours before the deadline to send automatic reminders.");
        }
    }

    private void apply(Event event, EventForm form) {
        event.setTitle(form.getTitle().trim());
        event.setDescription(form.getDescription());
        event.setStartAt(form.getStartAt());
        event.setDeadlineAt(form.getDeadlineAt());
        event.setDeadlineMode(form.getDeadlineMode());
        event.setAllowMultipleSubmissions(form.isAllowMultipleSubmissions());
        event.setAutoReminderEnabled(form.isAutoReminderEnabled());
        event.setReminderHoursBefore(form.isAutoReminderEnabled() ? form.getReminderHoursBefore() : null);
    }
}
