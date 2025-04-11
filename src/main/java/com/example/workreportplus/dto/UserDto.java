package com.example.workreportplus.dto;

import lombok.Data;

@Data
public class UserDto {
    private String nickname;
    private String password;
    private String email;
    private String role;
}
