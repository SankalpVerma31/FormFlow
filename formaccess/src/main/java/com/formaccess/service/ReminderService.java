package com.formaccess.service;

import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.Event;
import com.formaccess.entity.Reminder;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.DeliveryStatus;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.entity.enums.ReminderType;
import com.formaccess.exception.BusinessException;
import com.formaccess.mail.EmailService;
import com.formaccess.repository.EventRepository;
import com.formaccess.repository.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Finds eligible users who have not submitted and emails them.
 * Anti-spam:
 *   - manual reminders skip anyone reminded in the last app.reminder.min-gap-hours hours
 *   - automatic reminders are sent at most once per user per event
 */
@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final EligibilityService eligibilityService;
    private final ReminderRepository reminderRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    private final String baseUrl;
    private final int minGapHours;

    public ReminderService(EligibilityService eligibilityService, ReminderRepository reminderRepository,
                           EventRepository eventRepository, EmailService emailService, AuditService auditService,
                           @Value("${app.base-url}") String baseUrl,
                           @Value("${app.reminder.min-gap-hours:12}") int minGapHours) {
        this.eligibilityService = eligibilityService;
        this.reminderRepository = reminderRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        this.baseUrl = baseUrl;
        this.minGapHours = minGapHours;
    }

    /** Result shown to the creator, e.g. "Sent 2, skipped 1". */
    public record ReminderResult(int sent, int skipped) { }

    public List<Reminder> history(Event event) {
        return reminderRepository.findByEventOrderBySentAtDesc(event);
    }

    public int getMinGapHours() { return minGapHours; }

    @Transactional
    public ReminderResult sendManual(Event event, User creator) {
        if (event.getStatus() != EventStatus.OPEN) throw new BusinessException("Reminders can only be sent while the event is OPEN.");
        LocalDateTime cutoff = LocalDateTime.now().minusHours(minGapHours);
        int sent = 0, skipped = 0;
        for (User u : eligibilityService.findPendingUsers(event)) {
            if (reminderRepository.existsByEventAndUserAndSentAtAfter(event, u, cutoff)) {
                skipped++;
                continue;
            }
            deliver(event, u, creator, ReminderType.MANUAL);
            sent++;
        }
        auditService.log(event, creator, "REMINDER_SENT", "Manual reminder: sent " + sent + ", skipped " + skipped);
        return new ReminderResult(sent, skipped);
    }

    /** Called by ReminderScheduler. */
    @Transactional
    public void processAutomatic() {
        LocalDateTime now = LocalDateTime.now();
        for (Event event : eventRepository.findByStatusAndAutoReminderEnabledTrue(EventStatus.OPEN)) {
            if (event.getReminderHoursBefore() == null) continue;
            LocalDateTime windowStart = event.getDeadlineAt().minusHours(event.getReminderHoursBefore());
            if (now.isBefore(windowStart) || !now.isBefore(event.getDeadlineAt())) continue;

            int sent = 0;
            for (User u : eligibilityService.findPendingUsers(event)) {
                if (reminderRepository.existsByEventAndUserAndReminderType(event, u, ReminderType.AUTOMATIC)) continue;
                deliver(event, u, null, ReminderType.AUTOMATIC);
                sent++;
            }
            if (sent > 0) {
                auditService.log(event, null, "AUTO_REMINDER", "Automatic reminder sent to " + sent + " users");
                log.info("Automatic reminders for event {}: {}", event.getId(), sent);
            }
        }
    }

    private void deliver(Event event, User to, User sentBy, ReminderType type) {
        Reminder r = new Reminder();
        r.setEvent(event);
        r.setUser(to);
        r.setSentBy(sentBy);
        r.setReminderType(type);
        try {
            emailService.send(to.getEmail(),
                    "Reminder: " + event.getTitle(),
                    "Hi " + to.getFullName() + ",\n\n"
                            + "You have not yet filled \"" + event.getTitle() + "\".\n"
                            + "Deadline: " + event.getDeadlineAt().format(FMT) + " (IST)\n"
                            + "Open the form: " + baseUrl + "/e/" + event.getPublicToken() + "\n\n"
                            + "– FormAccess");
            r.setDeliveryStatus(DeliveryStatus.SENT);
        } catch (Exception e) {
            log.warn("Could not send reminder to {}: {}", to.getEmail(), e.getMessage());
            r.setDeliveryStatus(DeliveryStatus.FAILED);
        }
        reminderRepository.save(r);
    }
}
