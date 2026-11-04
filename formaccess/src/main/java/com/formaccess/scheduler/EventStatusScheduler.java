package com.formaccess.scheduler;

import com.formaccess.service.DeadlineService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Every minute: open SCHEDULED events whose start time arrived, close AUTOMATIC events past their deadline. */
@Component
public class EventStatusScheduler {

    private final DeadlineService deadlineService;

    public EventStatusScheduler(DeadlineService deadlineService) {
        this.deadlineService = deadlineService;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    public void run() {
        deadlineService.processAll();
    }
}
