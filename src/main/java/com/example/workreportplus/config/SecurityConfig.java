package com.example.workreportplus.config;

import com.example.jooq.tables.records.UsersRecord;
import com.example.workreportplus.service.AdminInitializerService;
import com.example.workreportplus.service.AuditLogService;
import com.example.workreportplus.service.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_POWER_USER = "POWER_USER";


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuditLogService auditLogService, UserService userService) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/logout"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/login", "/register", "/access-denied").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/**").hasAnyRole(ROLE_POWER_USER, ROLE_ADMIN)
                        .requestMatchers("/admin/**", "/read-sheets/**", "/users/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/sheets-viewer.html").hasRole(ROLE_ADMIN)
                        .requestMatchers("/audit-log/admin").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/audit-log/").hasAnyRole(ROLE_ADMIN, ROLE_POWER_USER, ROLE_USER)
                        .requestMatchers(HttpMethod.POST, "/audit-log/").hasAnyRole(ROLE_ADMIN, ROLE_POWER_USER, ROLE_USER)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthSuccessHandler(auditLogService, userService))
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return email -> {
            UsersRecord user = userService.getByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ApplicationRunner initAdmin(AdminInitializerService adminInitializerService) {
        return args -> adminInitializerService.initAdminUser();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthSuccessHandler(
            AuditLogService auditLogService,
            UserService userService
    ) {
        return (request, response, authentication) -> {
            String email = authentication.getName();
            UUID userId = userService.getUserIdByEmail(email).orElse(null);

            auditLogService.log(
                    "LOGIN",
                    "auth",
                    email,
                    userId,
                    request.getRemoteAddr(),
                    "User logged in successfully"
            );

            response.sendRedirect("/index");
        };
    }

}
