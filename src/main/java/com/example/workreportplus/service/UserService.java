package com.example.workreportplus.service;

import com.example.jooq.tables.Users;
import com.example.workreportplus.dto.UserDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final DSLContext dsl;

    public UserService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<UserDto> getAllUsers() {
        return dsl.select(
                        Users.USERS.NICKNAME,
                        Users.USERS.EMAIL,
                        Users.USERS.ROLE
                )
                .from(Users.USERS)
                .fetchInto(UserDto.class);
    }


    public void addOrUpdateUser(UserDto userDto) {
        dsl.insertInto(Users.USERS)
                .set(Users.USERS.NICKNAME, userDto.getNickname())
                .set(Users.USERS.EMAIL, userDto.getEmail())
                .set(Users.USERS.ROLE, userDto.getRole())
                .onConflict(Users.USERS.EMAIL)  // email as unique identifier
                .doUpdate()
                .set(Users.USERS.NICKNAME, userDto.getNickname())
                .set(Users.USERS.ROLE, userDto.getRole())
                .execute();
    }

    public Optional<String> getUserRole(String email) {
        String role = dsl.select(Users.USERS.ROLE)
                .from(Users.USERS)
                .where(Users.USERS.EMAIL.eq(email))
                .fetchOneInto(String.class);
        return Optional.ofNullable(role);
    }

}
