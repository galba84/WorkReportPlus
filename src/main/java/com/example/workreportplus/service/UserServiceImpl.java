package com.example.workreportplus.service;

import com.example.jooq.tables.records.UsersRecord;
import com.example.workreportplus.dto.UserDto;
import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.jooq.tables.Users.USERS;


@Service
public class UserServiceImpl implements UserService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(DSLContext dsl, PasswordEncoder passwordEncoder) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UsersRecord> getByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptional()
                .map(record -> {
                    UsersRecord user = new UsersRecord();
                    user.setId(record.getId());
                    user.setEmail(record.getEmail());
                    user.setPassword(record.getPassword());
                    user.setNickname(record.getNickname());
                    user.setRole(record.getRole());
                    return user;
                });
    }

    @Override
    public void createUser(UsersRecord user) {
        dsl.insertInto(USERS)
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD, passwordEncoder.encode(user.getPassword()))
                .set(USERS.NICKNAME, user.getNickname())
                .set(USERS.ROLE, user.getRole())
                .execute();
    }


    @Override
    public List<UserDto> getAllUsers() {
        return dsl.selectFrom(USERS)
                .fetchInto(UserDto.class);
    }

    @Override
    public void addOrUpdateUser(UserDto userDto) {
        Optional<UsersRecord> existing = getByEmail(userDto.getEmail());

        if (existing.isPresent()) {
            UsersRecord user = existing.get();

            dsl.attach(user); // 🛠️ ключовий момент

            user.setNickname(userDto.getNickname());
            user.setRole(userDto.getRole());

            if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }

            user.update();
        } else {
            UsersRecord user = dsl.newRecord(USERS);
            user.setId(UUID.randomUUID());
            user.setEmail(userDto.getEmail());
            user.setNickname(userDto.getNickname());
            user.setRole(userDto.getRole());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.insert();
        }
    }

    @Override
    public Optional<UUID> getUserIdByEmail(String email) {
        return dsl.select(USERS.ID)
                .from(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptional(USERS.ID);
    }




}
