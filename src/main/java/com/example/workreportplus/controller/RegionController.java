package com.example.workreportplus.controller;

import com.example.workreportplus.dto.RegionDto;
import com.example.workreportplus.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    // ✅ GET /api/regions?all=true|false
    @GetMapping
    public List<RegionDto> getRegions(@RequestParam(defaultValue = "false") boolean all) {
        return all ? regionService.getAllRegions() : regionService.getRegions();
    }


    // ✅ GET /api/regions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RegionDto> getRegionById(@PathVariable UUID id) {
        return regionService.getRegionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createRegion(@RequestBody RegionDto regionDto) {
        if (regionService.regionExistsByName(regionDto.getRegionName())) {
            return ResponseEntity.badRequest().body("Region with this name already exists.");
        }

        regionService.createRegion(regionDto);
        return ResponseEntity.ok("Region created successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateRegion(@PathVariable UUID id, @RequestBody RegionDto regionDto) {
        if (!regionService.regionExistsById(id)) {
            return ResponseEntity.notFound().build();
        }

        regionService.updateRegion(id, regionDto);
        return ResponseEntity.ok("Region updated successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRegion(@PathVariable UUID id) {
        if (!regionService.regionExistsById(id)) {
            return ResponseEntity.notFound().build();
        }

        regionService.softDeleteRegion(id);
        return ResponseEntity.ok("Region deleted successfully (status set to false).");
    }
}

