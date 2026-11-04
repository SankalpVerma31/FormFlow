package com.formaccess.controller;

import com.formaccess.entity.Submission;
import com.formaccess.entity.User;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.DashboardService;
import com.formaccess.service.SubmissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/participant")
public class ParticipantController {

    private final CurrentUserService currentUser;
    private final DashboardService dashboardService;
    private final SubmissionService submissionService;

    public ParticipantController(CurrentUserService currentUser, DashboardService dashboardService,
                                 SubmissionService submissionService) {
        this.currentUser = currentUser;
        this.dashboardService = dashboardService;
        this.submissionService = submissionService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("dash", dashboardService.participantDashboard(currentUser.get()));
        return "participant/dashboard";
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("submissions", submissionService.history(currentUser.get()));
        return "participant/history";
    }

    /** getOwnSubmission() refuses if the submission belongs to someone else. */
    @GetMapping("/submissions/{id}")
    public String view(@PathVariable Long id, Model model) {
        User user = currentUser.get();
        Submission s = submissionService.getOwnSubmission(user, id);
        model.addAttribute("event", s.getEvent());
        model.addAttribute("submission", s);
        model.addAttribute("lines", submissionService.answerLines(s));
        model.addAttribute("backUrl", "/participant/history");
        return "submission-view";
    }
}
