package com.example.workreportplus.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Controller
public class LanguageController {

    private final LocaleResolver localeResolver;

    public LanguageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping("/change-lang")
    public String changeLang(@RequestParam("lang") String lang, HttpServletRequest request) {
        Locale newLocale = new Locale(lang);

        // Ensure it's saved in session
        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, newLocale);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/index");
    }

}