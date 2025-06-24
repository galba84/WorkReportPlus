package com.example.workreportplus.controller;

import com.example.workreportplus.dto.UserDto;
import com.example.workreportplus.service.AuditLogService;
import com.example.workreportplus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UsersController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    /**
     * GET /api/users
     * Returns the full list of users as JSON
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * POST /api/users
     * Creates or updates a user from the JSON payload,
     * and logs the action. Returns the saved UserDto.
     */
    @PostMapping
    public ResponseEntity<UserDto> upsertUser(
            @RequestBody UserDto userDto,
            HttpServletRequest request
    ) {
        // Persist the user
        UserDto saved = userService.addOrUpdateUser(userDto);

        // Audit log
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        UUID currentUserId = userService.getUserIdByEmail(currentEmail)
                .orElse(null);
        String clientIp = request.getRemoteAddr();

        auditLogService.log(
                "USER_UPDATE",
                "users",
                userDto.getEmail(),  // entityId
                currentUserId,       // who did it
                clientIp,
                "Upsert via REST API"
        );

        return ResponseEntity.ok(saved);
    }

    /**
     * (Optional) GET /api/users/roles
     * Returns the roles of the currently authenticated user.
     */
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getUserRoles(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();
        return ResponseEntity.ok(roles);
    }
}
