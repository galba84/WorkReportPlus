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

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")                         // or whatever your API prefix is
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String,String>> ping() {
        // read Implementation-Version from MANIFEST.MF
        String version = Optional.ofNullable(
                getClass()
                        .getPackage()
                        .getImplementationVersion()
        ).orElse("dev");
        return ResponseEntity.ok(
                Map.of(
                        "pong",    "pong",
                        "version", version
                )
        );
    }
}