package com.formaccess.scheduler;

import com.formaccess.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Every 10 minutes: send automatic reminders for events that are close to their deadline. */
@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelay = 600_000, initialDelay = 30_000)
    public void run() {
        reminderService.processAutomatic();
    }
}
