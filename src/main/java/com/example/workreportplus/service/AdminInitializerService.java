package com.example.workreportplus.service;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static com.example.jooq.tables.Users.USERS;

/** Creates the configured bootstrap admin once. Never resets an existing password. */
@Service
public class AdminInitializerService implements ApplicationRunner {
    private final DSLContext dsl;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    public AdminInitializerService(DSLContext dsl, PasswordEncoder encoder,
            @Value("${app.admin.email}") String email, @Value("${app.admin.password}") String password) {
        this.dsl = dsl; this.encoder = encoder; this.email = email; this.password = password;
    }
    @Override public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Configure ADMIN_EMAIL and an ADMIN_PASSWORD of 12–72 bytes");
        }
        dsl.insertInto(USERS).set(USERS.EMAIL, email).set(USERS.NICKNAME, "Administrator")
                .set(USERS.ROLE, "ADMIN").set(USERS.PASSWORD, encoder.encode(password))
                .onConflict(USERS.EMAIL).doNothing().execute();
    }
}
