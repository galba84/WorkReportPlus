package com.example.workreportplus.controller;

import com.example.jooq.tables.records.UsersRecord;
import com.example.workreportplus.service.AuditLogService;
import com.example.workreportplus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AuthController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UsersRecord());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") UsersRecord formUser, Model model,
                                  HttpServletRequest request) {
        if (userService.getByEmail(formUser.getEmail()).isPresent()) {
            model.addAttribute("error", "User already exists");
            return "register";
        }

        // Set default role
        formUser.setRole("GUEST");

        UUID newUserId = userService.getByEmail(formUser.getEmail())
                .map(UsersRecord::getId)
                .orElse(null);

        auditLogService.log(
                "REGISTER",
                "auth",
                formUser.getEmail(),
                newUserId,
                request.getRemoteAddr(),
                "User registered with email " + formUser.getEmail()
        );

        // Save user via service
        userService.createUser(formUser);
        return "redirect:/login?registered";
    }
}
