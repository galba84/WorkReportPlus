package com.example.workreportplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WorkReportPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkReportPlusApplication.class, args);
    }

}
