package com.example.workreportplus.service;

import com.example.jooq.tables.Users;
import com.example.jooq.tables.records.UsersRecord;
import jakarta.annotation.PostConstruct;
import org.jooq.DSLContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminInitializerService {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    private static final Users USERS = Users.USERS;

    public AdminInitializerService(DSLContext dsl, PasswordEncoder passwordEncoder,
                                   AuditLogService auditLogService) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @PostConstruct
    public void initAdminUser() {
        String adminEmail = "verlenanatoly@gmail.com";
        String adminNickname = "SuperAdmin";
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 🔍 Перевіряємо, чи такий email вже є
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.EMAIL.eq(adminEmail))
        );

        if (!exists) {
            UsersRecord admin = dsl.newRecord(USERS);
            admin.setId(UUID.randomUUID());
            admin.setEmail(adminEmail);
            admin.setNickname(adminNickname);
            admin.setPassword(encodedPassword);
            admin.setRole("ADMIN");

            admin.insert();

            auditLogService.log(
                    "ADMIN_INIT",
                    "AdminInitializerService",
                    adminEmail, // entityId
                    null,       // userId (null because it's system-initiated)
                    "127.0.0.1", // or fetch from request if available
                    "Admin user initialized with email: " + adminEmail + " and password: " + rawPassword
            );
        } else {
            auditLogService.log(
                    "ADMIN_INIT_SKIP",
                    "AdminInitializerService",
                    adminEmail,
                    null,
                    "127.0.0.1",
                    "Admin user already exists, skipping initialization."
            );
        }
    }

}
