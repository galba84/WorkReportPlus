package com.example.workreportplus.controller;
import com.example.workreportplus.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final RegionService regionService;
    private final GroupService groupService;
    private final ContractorService contractorService;
    private final DescriptionTemplateService descriptionTemplateService;
    private final PlacesService placesService;
    private final RankService rankService;
    private final PositionService positionService;
    private final UnitService unitService;
    private final GoogleSheetsService sheets;

    @GetMapping("/integration")
    public Map<String,Boolean> integration() { return Map.of("googleEnabled", sheets.isEnabled()); }
    private void requireGoogle() {
        if (!sheets.isEnabled()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google integration is disabled");
    }
    @PostMapping("/regions") public void regions() throws IOException { requireGoogle(); regionService.updateRegionsFromTable(); }
    @PostMapping("/groups") public void groups() throws IOException { requireGoogle(); groupService.updateGroupsFromTable(); }
    @PostMapping("/contractors") public void contractors() throws IOException { requireGoogle(); contractorService.updateContractorsFromTable(); }
    @PostMapping("/groups/descriptions") public void descriptions() throws IOException { requireGoogle(); descriptionTemplateService.updateFromTableSource(); }
    @PostMapping("/places") public void places() throws IOException { requireGoogle(); placesService.updatePlacesFromTable(); }
    @PostMapping("/ranks") public void ranks() throws IOException { requireGoogle(); rankService.updateRanksFromTable(); }
    @PostMapping("/positions") public void positions() throws IOException { requireGoogle(); positionService.updatePositionsFromTable(); }
    @PostMapping("/units") public void units() throws IOException { requireGoogle(); unitService.updateUnitsFromTable(); }
    @PostMapping("/sync") public void sync() throws IOException {
        requireGoogle();
        positions(); units(); regions(); groups(); contractors(); descriptions(); places(); ranks();
    }
}
