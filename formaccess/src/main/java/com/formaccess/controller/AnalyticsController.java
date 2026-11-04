package com.formaccess.controller;

import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.EventService;
import com.formaccess.statistics.StatisticsService;
import com.formaccess.statistics.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/** Creator analytics page and the public/private summary settings. */
@Controller
@RequestMapping("/creator/events/{eventId}")
public class AnalyticsController {

    private final CurrentUserService currentUser;
    private final EventService eventService;
    private final StatisticsService statisticsService;
    private final SummaryService summaryService;

    public AnalyticsController(CurrentUserService currentUser, EventService eventService,
                               StatisticsService statisticsService, SummaryService summaryService) {
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.statisticsService = statisticsService;
        this.summaryService = summaryService;
    }

    @GetMapping("/analytics")
    public String analytics(@PathVariable Long eventId, Model model) {
        Event event = eventService.getManagedEvent(eventId, currentUser.get());
        model.addAttribute("event", event);
        model.addAttribute("stats", statisticsService.stats(event));
        model.addAttribute("summaryOptions", summaryService.options(event));
        return "creator/analytics";
    }

    @PostMapping("/summaries")
    public String saveSummaries(@PathVariable Long eventId, @RequestParam Map<String, String> params,
                                RedirectAttributes ra) {
        User user = currentUser.get();
        summaryService.save(eventService.getManagedEvent(eventId, user), params, user);
        ra.addFlashAttribute("success", "Public summary settings saved.");
        return "redirect:/creator/events/" + eventId + "/analytics#summaries";
    }
}
