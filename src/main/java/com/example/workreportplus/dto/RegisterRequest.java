package com.example.workreportplus.dto;

/**
 * @author Alex Sereda
 * @date 26.05.2025 14:09
 */
public record RegisterRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email
        @jakarta.validation.constraints.Size(max = 255) String email,
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 12, max = 72) String password,
        @jakarta.validation.constraints.Size(max = 50) String nickname) {}
