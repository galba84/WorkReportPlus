package com.example.workreportplus.controller;


import org.springframework.stereotype.Controller;
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
}
