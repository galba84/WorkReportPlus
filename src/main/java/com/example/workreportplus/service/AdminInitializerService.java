package com.example.workreportplus.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminInitializerService {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminInitializerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void initAdminUser() {
        String adminEmail = "verlenanatoly@gmail.com";
        String adminNickname = "SuperAdmin";

        // Check if admin user exists in 'users' table
        Integer adminCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ? AND role = 'ADMIN'",
                Integer.class, adminEmail
        );

        if (adminCount == null || adminCount == 0) {
            // Generate a UUID for the admin user
            UUID adminId = UUID.randomUUID();

            // Insert into 'users' table
            jdbcTemplate.update(
                    "INSERT INTO users (id, nickname, email, role) VALUES (?, ?, ?, ?)",
                    adminId, adminNickname, adminEmail, "ADMIN"
            );

            // Hash the email and insert into 'user_roles' table
            String hashedEmail = passwordEncoder.encode(adminEmail);
            jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, email_hash, role) VALUES (?, ?, ?)",
                    adminId, hashedEmail, "ADMIN"
            );

            System.out.println("✅ Admin user initialized.");
        } else {
            System.out.println("🔹 Admin user already exists, skipping initialization.");
        }
    }
}
