package com.studentmanagement.controller;

import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import com.studentmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout, Model model) {
        if (error != null)
            model.addAttribute("error", "Invalid username or password!");
        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors())
            return "register";
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            return "register";
        }
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("emailError", "Email already registered");
            return "register";
        }
        try {
            userService.createUser(user.getUsername(), user.getPassword(), user.getEmail(), user.getFullName(),
                    User.Role.STUDENT);
            redirectAttributes.addFlashAttribute("success", "Registration successful! You can now login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    // ========== FORGOT PASSWORD FLOW ==========

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes,
            Model model) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that email.");
            return "forgot-password";
        }
        User user = userOpt.get();
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // In production, send real email. For now, log to console.
        System.out.println("========================================");
        System.out.println("  OTP for " + email + ": " + otp);
        System.out.println("========================================");

        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("info", "OTP sent! Check your email (or console log for dev).");
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtp() {
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam String email, @RequestParam String otp,
            RedirectAttributes redirectAttributes, Model model) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email.");
            return "verify-otp";
        }
        User user = userOpt.get();
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            model.addAttribute("email", email);
            return "verify-otp";
        }
        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "OTP has expired. Please request a new one.");
            return "verify-otp";
        }
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("verified", true);
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email, @RequestParam String password,
            @RequestParam String confirmPassword, RedirectAttributes redirectAttributes, Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("email", email);
            return "reset-password";
        }
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email.");
            return "reset-password";
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login.");
        return "redirect:/login";
    }
}