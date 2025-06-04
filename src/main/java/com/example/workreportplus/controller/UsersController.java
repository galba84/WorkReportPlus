package com.example.workreportplus.controller;

import com.example.workreportplus.dto.UserDto;
import com.example.workreportplus.service.AuditLogService;
import com.example.workreportplus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UsersController {


    private final UserService userService;
    private final AuditLogService auditLogService;

    public UsersController(UserService usersService, AuditLogService auditLogService) {
        this.userService = usersService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String users(Model model) {
        List<UserDto> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/roles")
    public List<String> getUserRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @PostMapping("/add")
    public String addUser(
            UserDto userDto,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        userService.addOrUpdateUser(userDto);

        // Flash message для UI
        redirectAttributes.addFlashAttribute("infoMessage", "User updated successfully!");

        // Отримуємо поточного користувача
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID currentUserId = userService.getUserIdByEmail(currentEmail).orElse(null);

        String ip = request.getRemoteAddr();

        auditLogService.log(
                "USER_UPDATE",
                "users",
                userDto.getEmail(), // entityId — імейл того, кого оновили
                currentUserId,      // хто зробив дію
                ip,
                "Updated or created user via /add"
        );

        return "redirect:/users";
    }

}
