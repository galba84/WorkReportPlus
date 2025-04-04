package com.example.workreportplus.config;

import com.example.workreportplus.service.AdminInitializerService;
import com.example.workreportplus.service.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_POWER_USER = "POWER_USER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/logout"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/login", "/oauth2/**", "/access-denied").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/**").hasAnyRole(ROLE_POWER_USER, ROLE_ADMIN)
                        .requestMatchers("/admin/**", "/read-sheets/**", "/users/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/sheets-viewer.html").hasRole(ROLE_ADMIN)
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .defaultSuccessUrl("/index", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            request.getSession().invalidate();
                            response.sendRedirect("https://accounts.google.com/Logout");
                        })
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
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(UserService userService) {
        return userRequest -> {
            OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
            String email = oAuth2User.getAttribute("email");

            if (email == null) {
                throw new IllegalStateException("OAuth2 user email not found");
            }

            Optional<String> userRole = userService.getUserRole(email);
            Set<GrantedAuthority> authorities = userRole
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .stream().collect(Collectors.toSet());

            // Spring convention style
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
            }


            return new CustomOAuth2User(oAuth2User, authorities);
        };
    }

    @Bean
    public ApplicationRunner initAdmin(AdminInitializerService adminInitializerService) {
        return args -> adminInitializerService.initAdminUser();
    }



}
