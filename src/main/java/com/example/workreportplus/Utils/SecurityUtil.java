package com.example.workreportplus.Utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class SecurityUtil {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return "Anonymous"; // or throw exception depending on your logic
        }

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            // extract email from OAuth2User attributes
            return (String) oauth2User.getAttributes().get("email");
        }

        return authentication.getName(); // fallback for other kinds of authentication
    }
}

