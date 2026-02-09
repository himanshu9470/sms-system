// AuthController.java
package com.studentmanagement.controller;

import com.studentmanagement.model.User;
import com.studentmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "register";
        }
        
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            return "register";
        }
        
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("emailError", "Email already registered");
            return "register";
        }
        
        try {
            userService.createUser(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFullName(),
                User.Role.STUDENT
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! You can now login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}