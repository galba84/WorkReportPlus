package com.example.workreportplus.dto;

import java.util.UUID;

/**
 * @author Alex Sereda
 * @date 26.05.2025 14:10
 */
public record AuthResponse(UUID userId, String email, String role) {}
