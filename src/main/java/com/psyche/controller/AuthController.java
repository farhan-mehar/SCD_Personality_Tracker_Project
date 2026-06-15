package com.psyche.controller;

import com.psyche.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired private UserService userService;

    @GetMapping("/")
    public String root() { return "redirect:/login"; }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String error,
                            @RequestParam(required=false) String logout,
                            Model m) {
        if (error != null)  m.addAttribute("error",  "Invalid email or password. Please try again.");
        if (logout != null) m.addAttribute("logout", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() { return "signup"; }

    @PostMapping("/signup")
    public String signup(@RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         RedirectAttributes ra) {

        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            ra.addFlashAttribute("error", "All fields are required.");
            return "redirect:/signup";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/signup";
        }
        if (password.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/signup";
        }
        if (userService.emailExists(email)) {
            ra.addFlashAttribute("error", "An account with this email already exists.");
            return "redirect:/signup";
        }

        userService.register(fullName, email, password);
        ra.addFlashAttribute("success", "Account created! Please log in.");
        return "redirect:/login";
    }
}
