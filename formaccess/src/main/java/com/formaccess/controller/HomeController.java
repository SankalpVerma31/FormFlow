package com.formaccess.controller;

import com.formaccess.security.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Landing page and the role-based redirect after login. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal AppUserPrincipal principal) {
        return principal == null ? "index" : "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserPrincipal principal) {
        return switch (principal.getRole()) {
            case "ADMIN" -> "redirect:/admin";
            case "CREATOR" -> "redirect:/creator";
            default -> "redirect:/participant";
        };
    }
}
