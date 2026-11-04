package com.formaccess.controller;

import com.formaccess.dto.RegisterForm;
import com.formaccess.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Login page and registration. The actual login check (POST /login) is done by
 * Spring Security itself, not by our code.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        model.addAttribute("departments", userService.allDepartments());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result, Model model) {
        // @Valid already checked the annotations; now the checks that need logic or the database.
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (userService.emailTaken(form.getEmail())) {
            result.rejectValue("email", "taken", "That email is already registered");
        }
        if (result.hasErrors()) {
            model.addAttribute("departments", userService.allDepartments());
            return "auth/register";
        }
        userService.register(form);
        return "redirect:/login?registered";
    }
}
