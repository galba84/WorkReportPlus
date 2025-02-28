package com.example.workreportplus.controller;


import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    // 1️⃣ Home Page
     @GetMapping("/")
     public String home() {
         return "index";
     }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "403"; // Return a custom 403 page
            }
        }
        return "error"; // Return a generic error page
    }
}
