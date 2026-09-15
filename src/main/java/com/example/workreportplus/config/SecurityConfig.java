package com.example.workreportplus.config;

import com.example.workreportplus.service.UserService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_POWER_USER = "POWER_USER";

    @Bean
    public SecretKey jwtSigningKey(@Value("${app.jwt.secret}") String secret, Environment environment) {
        if (secret.isBlank() && environment.acceptsProfiles(Profiles.of("demo"))) {
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes outside the demo profile");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtFilter,
            @Value("${app.cors.origins}") String origins) throws Exception {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(source))
                // Authentication uses an Authorization bearer header, never a session cookie.
                .csrf(c -> c.disable())
                .authorizeHttpRequests(a -> a
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/login", "/api/auth/register", "/api/ping").permitAll()
                    .requestMatchers("/api/auth/me", "/api/users/roles").authenticated()
                    .requestMatchers("/api/users/**", "/api/settings/**", "/api/audit-log/**",
                            "/api/admin/**", "/admin/**", "/read-sheets").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "POWER_USER", "USER")
                    .requestMatchers(HttpMethod.POST, "/api/daily-work-report").hasAnyRole("ADMIN", "POWER_USER")
                    .requestMatchers("/api/**").hasRole("ADMIN")
                    .anyRequest().denyAll())
                .exceptionHandling(e -> e
                    .authenticationEntryPoint((req, res, ex) -> res.sendError(401))
                    .accessDeniedHandler((req, res, ex) -> res.sendError(403)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(b -> b.disable()).formLogin(f -> f.disable()).build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Run once, inside Spring Security's filter chain.
        return registration;
    }

    @Bean
    public UserDetailsService userDetailsService(UserService users) {
        return email -> {
            var user = users.getByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return User.withUsername(user.getEmail()).password(user.getPassword())
                    .roles(user.getRole()).build();
        };
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
