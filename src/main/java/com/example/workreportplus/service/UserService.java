package com.example.workreportplus.service;

import com.example.jooq.tables.records.UsersRecord;
import com.example.workreportplus.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<UsersRecord> getByEmail(String email);
    void createUser(UsersRecord user);
    List<UserDto> getAllUsers();
    void addOrUpdateUser(UserDto userDto);

    Optional<UUID> getUserIdByEmail(String currentEmail);
}
