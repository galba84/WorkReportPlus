package com.example.workreportplus.dto;

import lombok.Data;

@Data
public class UserDto {
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 50)
    private String nickname;
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Email
    @jakarta.validation.constraints.Size(max = 255)
    private String email;
    private String role;
}
