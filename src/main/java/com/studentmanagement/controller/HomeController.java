package com.studentmanagement.controller;

import com.studentmanagement.model.User;
import com.studentmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            var userOpt = userService.getUserByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                switch (user.getRole()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case FACULTY:
                    return "redirect:/faculty/dashboard";
                case PARENT:
                    return "redirect:/parent/dashboard";
                case STUDENT:
                    return "redirect:/student/dashboard";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}