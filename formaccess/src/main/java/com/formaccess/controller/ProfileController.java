package com.formaccess.controller;

import com.formaccess.dto.PasswordForm;
import com.formaccess.dto.ProfileForm;
import com.formaccess.entity.User;
import com.formaccess.security.CurrentUserService;
import com.formaccess.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final CurrentUserService currentUser;
    private final UserService userService;

    public ProfileController(CurrentUserService currentUser, UserService userService) {
        this.currentUser = currentUser;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = currentUser.get();
        model.addAttribute("user", user);
        model.addAttribute("profileForm", userService.toProfileForm(user));
        model.addAttribute("passwordForm", new PasswordForm());
        model.addAttribute("departments", userService.allDepartments());
        return "profile";
    }

    @PostMapping("/profile")
    public String update(@Valid @ModelAttribute("profileForm") ProfileForm form, BindingResult result,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Please check your details: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile";
        }
        userService.updateProfile(currentUser.get(), form);
        ra.addFlashAttribute("success", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form, BindingResult result,
                                 RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile";
        }
        userService.changePassword(currentUser.get(), form);
        ra.addFlashAttribute("success", "Password changed.");
        return "redirect:/profile";
    }
}
