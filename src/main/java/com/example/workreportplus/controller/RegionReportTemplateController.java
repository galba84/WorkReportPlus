package com.example.workreportplus.controller;

import com.example.workreportplus.dto.RegionReportTemplateDto;
import com.example.workreportplus.service.RegionReportTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Alex Sereda
 * @date 10.06.2025 17:39
 */
@RestController
@RequestMapping("/api/region-report-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RegionReportTemplateController {

    private final RegionReportTemplateService service;

    @PostMapping
    public RegionReportTemplateDto create(@RequestBody RegionReportTemplateDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public RegionReportTemplateDto getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping
    public List<RegionReportTemplateDto> getAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public RegionReportTemplateDto update(@PathVariable UUID id, @RequestBody RegionReportTemplateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
