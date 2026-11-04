package com.formaccess.controller.api;

import com.formaccess.dto.EventStats;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.EventService;
import com.formaccess.statistics.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * A REST API endpoint: returns JSON instead of an HTML page.
 * The analytics page's JavaScript calls it with fetch() and draws Chart.js graphs.
 * Only the event's creator (or an admin) gets the data – checked by getManagedEvent().
 */
@RestController
public class StatsApiController {

    private final CurrentUserService currentUser;
    private final EventService eventService;
    private final StatisticsService statisticsService;

    public StatsApiController(CurrentUserService currentUser, EventService eventService,
                              StatisticsService statisticsService) {
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/api/creator/events/{eventId}/stats")
    public EventStats stats(@PathVariable Long eventId) {
        return statisticsService.stats(eventService.getManagedEvent(eventId, currentUser.get()));
    }
}
