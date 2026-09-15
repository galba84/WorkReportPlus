package com.example.workreportplus.dto;

/**
 * @author Alex Sereda
 * @date 26.05.2025 14:09
 */
public record LoginRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.NotBlank String password) {}
