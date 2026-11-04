package com.formaccess.controller;

import com.formaccess.eligibility.EligibilityService;
import com.formaccess.entity.Event;
import com.formaccess.entity.Submission;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.EventStatus;
import com.formaccess.exception.FormValidationException;
import com.formaccess.exception.NotFoundException;
import com.formaccess.exception.SubmissionBlockedException;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.AccessResult;
import com.formaccess.service.EventService;
import com.formaccess.service.SubmissionService;
import com.formaccess.statistics.SummaryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * The participant side of a form: /e/{token}
 * Having the link is NOT enough – SubmissionService.checkAccess() runs on every request.
 */
@Controller
@RequestMapping("/e/{token}")
public class FormController {

    private final CurrentUserService currentUser;
    private final EventService eventService;
    private final SubmissionService submissionService;
    private final SummaryService summaryService;
    private final EligibilityService eligibilityService;

    public FormController(CurrentUserService currentUser, EventService eventService,
                          SubmissionService submissionService, SummaryService summaryService,
                          EligibilityService eligibilityService) {
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.submissionService = submissionService;
        this.summaryService = summaryService;
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    public String open(@PathVariable String token, Model model, HttpServletResponse response) {
        User user = currentUser.get();
        Event event = eventService.getByToken(token);
        AccessResult access = submissionService.checkAccess(user, event);
        if (access != AccessResult.ALLOWED) {
            return statusPage(event, user, access, model, response);
        }
        model.addAttribute("event", event);
        model.addAttribute("inputs", submissionService.inputViews(event, null, null));
        return "form/fill";
    }

    @PostMapping
    public String submit(@PathVariable String token, @RequestParam MultiValueMap<String, String> params,
                         Model model, HttpServletResponse response) {
        User user = currentUser.get();
        Event event = eventService.getByToken(token);
        try {
            Submission s = submissionService.submit(user, event, params);
            return "redirect:/e/" + token + "/done/" + s.getId();
        } catch (FormValidationException e) {
            // The failed transaction was rolled back, so load the event again before showing the form.
            event = eventService.getByToken(token);
            model.addAttribute("event", event);
            model.addAttribute("inputs", submissionService.inputViews(event, params, e.getErrors()));
            model.addAttribute("error", e.getMessage());
            return "form/fill";
        } catch (SubmissionBlockedException e) {
            event = eventService.getByToken(token);
            return statusPage(event, user, e.getReason(), model, response);
        }
    }

    @GetMapping("/done/{submissionId}")
    public String done(@PathVariable String token, @PathVariable Long submissionId, Model model) {
        Submission s = submissionService.getOwnSubmission(currentUser.get(), submissionId);
        if (!s.getEvent().getPublicToken().equals(token)) throw new NotFoundException("Submission not found");
        model.addAttribute("event", s.getEvent());
        model.addAttribute("submission", s);
        return "form/done";
    }

    /** Public summaries – only eligible users (or the creator/admin) may look. */
    @GetMapping("/results")
    public String results(@PathVariable String token, Model model) {
        User user = currentUser.get();
        Event event = eventService.getByToken(token);
        if (event.getStatus() == EventStatus.DRAFT) throw new NotFoundException("Form not found");
        if (!submissionService.canManage(user, event) && !eligibilityService.isEligible(user, event)) {
            throw new AccessDeniedException("You are not eligible for this event.");
        }
        model.addAttribute("event", event);
        model.addAttribute("results", summaryService.publicResults(event));
        return "form/results";
    }

    private String statusPage(Event event, User user, AccessResult access, Model model, HttpServletResponse response) {
        if (access == AccessResult.NOT_PUBLISHED) throw new NotFoundException("Form not found");
        if (access == AccessResult.NOT_ELIGIBLE || access == AccessResult.ACCOUNT_INACTIVE) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        model.addAttribute("event", event);
        model.addAttribute("access", access);
        model.addAttribute("canManage", submissionService.canManage(user, event));
        model.addAttribute("mySubmissions", submissionService.mySubmissionsFor(user, event));
        return "form/status";
    }
}
