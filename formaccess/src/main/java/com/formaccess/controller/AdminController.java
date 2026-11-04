package com.formaccess.controller;

import com.formaccess.dto.AdminUserForm;
import com.formaccess.dto.EventProgress;
import com.formaccess.entity.Event;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.DashboardService;
import com.formaccess.service.EventService;
import com.formaccess.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/** Admin pages. SecurityConfig already guarantees only ADMIN users reach /admin/**. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final EventService eventService;
    private final CurrentUserService currentUser;

    public AdminController(DashboardService dashboardService, UserService userService,
                           EventService eventService, CurrentUserService currentUser) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.eventService = eventService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("counts", dashboardService.adminCounts());
        model.addAttribute("activity", dashboardService.recentActivity());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.allUsers());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("userForm", new AdminUserForm());
        model.addAttribute("departments", userService.allDepartments());
        return "admin/user-form";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("userForm", userService.toAdminForm(userService.getById(id)));
        model.addAttribute("departments", userService.allDepartments());
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("userForm") AdminUserForm form, BindingResult result,
                           Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("departments", userService.allDepartments());
            return "admin/user-form";
        }
        userService.adminSave(form, currentUser.get());
        ra.addFlashAttribute("success", "User saved.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleStatus(id, currentUser.get());
        ra.addFlashAttribute("success", "Account status changed.");
        return "redirect:/admin/users";
    }

    @GetMapping("/events")
    public String events(Model model) {
        List<EventProgress> list = new ArrayList<>();
        for (Event e : eventService.allEvents()) list.add(eventService.progress(e));
        model.addAttribute("events", list);
        return "admin/events";
    }
}
