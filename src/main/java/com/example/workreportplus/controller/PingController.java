package com.example.workreportplus.controller;

/**
 * @author Alex Sereda
 * @date 23.06.2025 9:19
 */
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")                         // or whatever your API prefix is
@CrossOrigin(origins = "http://localhost:5178") // allow your Vite dev server
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        // you could also return a JSON object if you prefer
        return ResponseEntity.ok("pong");
    }
}