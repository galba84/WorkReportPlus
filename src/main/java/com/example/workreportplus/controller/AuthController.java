package com.example.workreportplus.controller;

import com.example.jooq.tables.records.UsersRecord;
import com.example.workreportplus.config.JwtTokenProvider;
import com.example.workreportplus.dto.AuthResponse;
import com.example.workreportplus.dto.LoginRequest;
import com.example.workreportplus.dto.RegisterRequest;
import com.example.workreportplus.service.AuditLogService;
import com.example.workreportplus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuditLogService auditLogService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, AuditLogService auditLogService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        UsersRecord user = userService.getByEmail(email).orElseThrow();

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );


        UsersRecord user = userService.getByEmail(request.email()).orElseThrow();
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());

        auditLogService.log(
                "LOGIN", "auth", user.getEmail(), user.getId(), httpRequest.getRemoteAddr(), "User logged in"
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", new AuthResponse(user.getId(), user.getEmail(), user.getRole())
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        if (userService.getByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        UsersRecord user = new UsersRecord();
        user.setEmail(request.email());
        user.setPassword(userService.encodePassword(request.password()));
        user.setRole("GUEST");

        userService.createUser(user);

        UUID userId = userService.getByEmail(request.email()).map(UsersRecord::getId).orElse(null);
        auditLogService.log("REGISTER", "auth", user.getEmail(), userId, httpRequest.getRemoteAddr(), "User registered");

        return ResponseEntity.ok("User registered successfully");
    }
}
