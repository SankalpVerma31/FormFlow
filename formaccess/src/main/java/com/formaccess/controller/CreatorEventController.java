package com.formaccess.controller;

import com.formaccess.dto.EventForm;
import com.formaccess.dto.QuestionForm;
import com.formaccess.dto.RuleForm;
import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.Event;
import com.formaccess.entity.Submission;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.repository.AuditLogRepository;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Creator pages: dashboard, create/edit event, the event "manage" page,
 * publish/close/delete, reminders, preview and submissions.
 * Every method that takes an event id calls eventService.getManagedEvent(...),
 * which checks on the server that the user owns the event (or is admin).
 */
@Controller
@RequestMapping("/creator")
public class CreatorEventController {

    private final CurrentUserService currentUser;
    private final EventService eventService;
    private final DashboardService dashboardService;
    private final RuleService ruleService;
    private final EligibilityService eligibilityService;
    private final ReminderService reminderService;
    private final SubmissionService submissionService;
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final String baseUrl;

    public CreatorEventController(CurrentUserService currentUser, EventService eventService,
                                  DashboardService dashboardService, RuleService ruleService,
                                  EligibilityService eligibilityService, ReminderService reminderService,
                                  SubmissionService submissionService, AuditLogRepository auditLogRepository,
                                  UserService userService,
                                  @Value("${app.base-url}") String baseUrl) {
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.dashboardService = dashboardService;
        this.ruleService = ruleService;
        this.eligibilityService = eligibilityService;
        this.reminderService = reminderService;
        this.submissionService = submissionService;
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
        this.baseUrl = baseUrl;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("events", dashboardService.creatorEvents(currentUser.get()));
        return "creator/dashboard";
    }

    // ---------------------------------------------------------------- create / edit

    @GetMapping("/events/new")
    public String newEvent(Model model) {
        EventForm form = new EventForm();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1);
        form.setStartAt(now);
        form.setDeadlineAt(now.plusDays(7));
        model.addAttribute("eventForm", form);
        model.addAttribute("isNew", true);
        return "creator/event-form";
    }

    @PostMapping("/events")
    public String create(@Valid @ModelAttribute("eventForm") EventForm form, BindingResult result, Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("isNew", true);
            return "creator/event-form";
        }
        Event event = eventService.create(form, currentUser.get());
        ra.addFlashAttribute("success", "Event created. Now add questions and eligibility rules.");
        return "redirect:/creator/events/" + event.getId();
    }

    @GetMapping("/events/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Event event = eventService.getManagedEvent(id, currentUser.get());
        model.addAttribute("event", event);
        model.addAttribute("eventForm", eventService.toForm(event));
        model.addAttribute("isNew", false);
        return "creator/event-form";
    }

    @PostMapping("/events/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("eventForm") EventForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        User user = currentUser.get();
        Event event = eventService.getManagedEvent(id, user);
        if (result.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("isNew", false);
            return "creator/event-form";
        }
        eventService.update(event, form, user);
        ra.addFlashAttribute("success", "Event updated.");
        return "redirect:/creator/events/" + id;
    }

    // ---------------------------------------------------------------- manage page

    @GetMapping("/events/{id}")
    public String manage(@PathVariable Long id, Model model) {
        Event event = eventService.getManagedEvent(id, currentUser.get());
        model.addAttribute("event", event);
        model.addAttribute("progress", eventService.progress(event));
        model.addAttribute("shareLink", baseUrl + "/e/" + event.getPublicToken());
        model.addAttribute("questionForm", new QuestionForm());
        RuleForm ruleForm = new RuleForm();
        ruleForm.setGroupNo(event.getRules().isEmpty() ? 1 : event.getRules().get(event.getRules().size() - 1).getGroupNo());
        model.addAttribute("ruleForm", ruleForm);
        model.addAttribute("nextGroupNo", ruleService.nextGroupNo(event));
        model.addAttribute("ruleGroups", ruleService.grouped(event));
        model.addAttribute("pendingUsers", event.getStatus() == EventStatus.DRAFT
                ? List.of() : eligibilityService.findPendingUsers(event));
        model.addAttribute("reminders", reminderService.history(event));
        model.addAttribute("reminderGap", reminderService.getMinGapHours());
        model.addAttribute("auditLogs", auditLogRepository.findByEventOrderByCreatedAtDesc(event));
        return "creator/event-manage";
    }

    @PostMapping("/events/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes ra) {
        User user = currentUser.get();
        Event event = eventService.getManagedEvent(id, user);
        eventService.publish(event, user);
        ra.addFlashAttribute("success", "Published! Status: " + event.getStatus() + ". Share the link with participants.");
        return "redirect:/creator/events/" + id;
    }

    @PostMapping("/events/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes ra) {
        User user = currentUser.get();
        eventService.closeManually(eventService.getManagedEvent(id, user), user);
        ra.addFlashAttribute("success", "Submissions closed.");
        return "redirect:/creator/events/" + id;
    }

    @PostMapping("/events/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        User user = currentUser.get();
        eventService.delete(eventService.getManagedEvent(id, user), user);
        ra.addFlashAttribute("success", "Event deleted.");
        return "redirect:/creator";
    }

    @PostMapping("/events/{id}/reminders")
    public String sendReminders(@PathVariable Long id, RedirectAttributes ra) {
        User user = currentUser.get();
        ReminderService.ReminderResult r = reminderService.sendManual(eventService.getManagedEvent(id, user), user);
        ra.addFlashAttribute("success", "Reminders sent: " + r.sent() + ". Skipped (reminded recently): " + r.skipped() + ".");
        return "redirect:/creator/events/" + id + "#reminders";
    }

    // ---------------------------------------------------------------- preview & submissions

    @GetMapping("/events/{id}/preview")
    public String preview(@PathVariable Long id, Model model) {
        Event event = eventService.getManagedEvent(id, currentUser.get());
        model.addAttribute("event", event);
        model.addAttribute("inputs", submissionService.inputViews(event, null, null));
        return "creator/preview";
    }

    @GetMapping("/events/{id}/submissions")
    public String submissions(@PathVariable Long id, Model model) {
        Event event = eventService.getManagedEvent(id, currentUser.get());
        model.addAttribute("event", event);
        model.addAttribute("submissions", submissionService.submissionsOf(event));
        return "creator/submissions";
    }

    @GetMapping("/events/{id}/submissions/{sid}")
    public String submission(@PathVariable Long id, @PathVariable Long sid, Model model) {
        Event event = eventService.getManagedEvent(id, currentUser.get());
        Submission s = submissionService.getSubmissionForManager(event, sid);
        model.addAttribute("event", event);
        model.addAttribute("submission", s);
        model.addAttribute("lines", submissionService.answerLines(s));
        model.addAttribute("backUrl", "/creator/events/" + id + "/submissions");
        return "submission-view";
    }

    /** Department list for the rule builder dropdown (added to every page of this controller). */
    @ModelAttribute("departments")
    public Object departments() {
        return userService.allDepartments();
    }
}
