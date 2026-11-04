package com.formaccess.controller;

import com.formaccess.dto.QuestionForm;
import com.formaccess.dto.RuleForm;
import com.formaccess.entity.Event;
import com.formaccess.entity.Question;
import com.formaccess.entity.User;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.EventService;
import com.formaccess.service.FormService;
import com.formaccess.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** The form builder (questions) and the rule builder (eligibility conditions). */
@Controller
@RequestMapping("/creator/events/{eventId}")
public class FormBuilderController {

    private final CurrentUserService currentUser;
    private final EventService eventService;
    private final FormService formService;
    private final RuleService ruleService;

    public FormBuilderController(CurrentUserService currentUser, EventService eventService,
                                 FormService formService, RuleService ruleService) {
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.formService = formService;
        this.ruleService = ruleService;
    }

    // ---------------------------------------------------------------- questions

    @PostMapping("/questions")
    public String addQuestion(@PathVariable Long eventId, @Valid @ModelAttribute QuestionForm form,
                              BindingResult result, RedirectAttributes ra) {
        User user = currentUser.get();
        Event event = eventService.getManagedEvent(eventId, user);
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Question not added: " + result.getAllErrors().get(0).getDefaultMessage());
        } else {
            formService.addQuestion(event, form, user);
            ra.addFlashAttribute("success", "Question added.");
        }
        return "redirect:/creator/events/" + eventId + "#questions";
    }

    @GetMapping("/questions/{questionId}/edit")
    public String editQuestion(@PathVariable Long eventId, @PathVariable Long questionId, Model model) {
        Event event = eventService.getManagedEvent(eventId, currentUser.get());
        Question q = formService.getQuestion(event, questionId);
        model.addAttribute("event", event);
        model.addAttribute("question", q);
        model.addAttribute("questionForm", formService.toForm(q));
        return "creator/question-edit";
    }

    @PostMapping("/questions/{questionId}/edit")
    public String updateQuestion(@PathVariable Long eventId, @PathVariable Long questionId,
                                 @Valid @ModelAttribute("questionForm") QuestionForm form, BindingResult result,
                                 Model model, RedirectAttributes ra) {
        User user = currentUser.get();
        Event event = eventService.getManagedEvent(eventId, user);
        Question q = formService.getQuestion(event, questionId);
        if (result.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("question", q);
            return "creator/question-edit";
        }
        formService.updateQuestion(event, q, form, user);
        ra.addFlashAttribute("success", "Question updated.");
        return "redirect:/creator/events/" + eventId + "#questions";
    }

    @PostMapping("/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long eventId, @PathVariable Long questionId, RedirectAttributes ra) {
        User user = currentUser.get();
        Event event = eventService.getManagedEvent(eventId, user);
        formService.deleteQuestion(event, formService.getQuestion(event, questionId), user);
        ra.addFlashAttribute("success", "Question deleted.");
        return "redirect:/creator/events/" + eventId + "#questions";
    }

    @PostMapping("/questions/{questionId}/move")
    public String move(@PathVariable Long eventId, @PathVariable Long questionId, @RequestParam String dir) {
        Event event = eventService.getManagedEvent(eventId, currentUser.get());
        formService.move(event, formService.getQuestion(event, questionId), "up".equals(dir) ? -1 : 1);
        return "redirect:/creator/events/" + eventId + "#questions";
    }

    // ---------------------------------------------------------------- eligibility rules

    @PostMapping("/rules")
    public String addRule(@PathVariable Long eventId, @Valid @ModelAttribute RuleForm form,
                          BindingResult result, RedirectAttributes ra) {
        User user = currentUser.get();
        Event event = eventService.getManagedEvent(eventId, user);
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Rule not added: " + result.getAllErrors().get(0).getDefaultMessage());
        } else {
            ruleService.addRule(event, form, user);
            ra.addFlashAttribute("success", "Condition added.");
        }
        return "redirect:/creator/events/" + eventId + "#eligibility";
    }

    @PostMapping("/rules/{ruleId}/delete")
    public String deleteRule(@PathVariable Long eventId, @PathVariable Long ruleId, RedirectAttributes ra) {
        User user = currentUser.get();
        ruleService.deleteRule(eventService.getManagedEvent(eventId, user), ruleId, user);
        ra.addFlashAttribute("success", "Condition removed.");
        return "redirect:/creator/events/" + eventId + "#eligibility";
    }
}
