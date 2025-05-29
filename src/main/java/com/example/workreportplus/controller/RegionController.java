package com.example.workreportplus.controller;

import com.example.workreportplus.dto.RegionDto;
import com.example.workreportplus.service.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Alex Sereda
 * @date 27.05.2025 10:06
 */

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping
    public List<RegionDto> getRegions() {
        return regionService.getRegions();
    }
}
