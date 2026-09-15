package com.example.workreportplus.controller;

/**
 * @author Alex Sereda
 * @date 27.06.2025 21:21
 */

import com.example.workreportplus.dto.AppSettingDto;
import com.example.workreportplus.service.AppSettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.example.workreportplus.service.AppSettingsService.*;


@RestController
@RequestMapping("/api/settings")
public class AppSettingsController {

    private final AppSettingsService appSettingsService;


    public static final String VOCABULARY_UPDATE_JOB = "vocabulary_update_job";
    private static final Map<String, String> DEFAULT_METADATA = Map.of(
            OPERATIVE_REPORT_BPLA, "map",
            OPERATIVE_REPORT_REB, "map",
            ATTENDANCE_MAP, "map",
            VOCABULARY_UPDATE_JOB, "boolean"
    );

    public AppSettingsController(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @GetMapping
    public List<AppSettingDto> getList() {
        return appSettingsService.findAll();
    }

    @GetMapping("/{key}")
    public ResponseEntity<AppSettingDto> getById(@PathVariable String key) {
        return appSettingsService.findByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AppSettingDto> create(@RequestBody AppSettingDto input) {
        AppSettingDto dto = appSettingsService.create(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{key}")
    public ResponseEntity<AppSettingDto> update(
            @PathVariable String key,
            @RequestBody AppSettingDto input
    ) {
        AppSettingDto dto = appSettingsService.update(key, input);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/metadata")
    public Map<String, String> getMetadata() {
        return DEFAULT_METADATA;
    }
}