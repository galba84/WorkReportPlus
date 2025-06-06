package com.example.workreportplus.controller;

import com.example.workreportplus.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ContractorService contractorService;
    private final GroupService groupService;
    private final RegionService regionService;
    private final DescriptionTemplateService descriptionTemplateService;
    private final PlacesService placesService;
    private final RankService rankService;
    private final PositionService positionService;
    private final UnitService unitService;

    public AdminController(ContractorService contractorService, GroupService groupService,
                           RegionService regionService, DescriptionTemplateService descriptionTemplateService,
                           PlacesService placesService, RankService rankService, PositionService positionService,
                           UnitService unitService) {
        this.contractorService = contractorService;
        this.groupService = groupService;
        this.regionService = regionService;
        this.descriptionTemplateService = descriptionTemplateService;
        this.placesService = placesService;
        this.rankService = rankService;
        this.positionService = positionService;
        this.unitService = unitService;
    }

    @PostMapping("/contractors")
    public ResponseEntity<String> updateContractorsFromTable() throws IOException {
        contractorService.updateContractorsFromTable();
        return ResponseEntity.ok("Contractors updated successfully!");
    }

    @PostMapping("/groups")
    public ResponseEntity<String> updateGroupsFromTable() throws IOException {
        groupService.updateGroupsFromTable();
        return ResponseEntity.ok("Groups updated successfully!");
    }

    @PostMapping("/regions")
    public ResponseEntity<String> updateRegionsFromTable() throws IOException {
        regionService.updateRegionsFromTable();
        return ResponseEntity.ok("Regions updated successfully!");
    }

    @PostMapping("/groups/descriptions")
    public ResponseEntity<String> updateGroupDescription() throws IOException {
        descriptionTemplateService.updateFromTableSource();
        return ResponseEntity.ok("Шаблони звітів груп updated successfully!");
    }

    @PostMapping("/places")
    public ResponseEntity<String> updatePlacesFromTable() throws IOException {
        placesService.updatePlacesFromTable();
        return ResponseEntity.ok("Places updated successfully!");
    }

    @PostMapping("/ranks")
    public ResponseEntity<String> updateRanksFromTable() throws IOException {
        rankService.updateRanksFromTable();
        return ResponseEntity.ok("Ranks updated successfully!");
    }

    @PostMapping("/positions")
    public ResponseEntity<String> updatePositionsFromTable() throws IOException {
        positionService.updatePositionsFromTable();
        return ResponseEntity.ok("Positions updated successfully!");
    }

    @PostMapping("/units")
    public ResponseEntity<String> updateUnitsFromTable() throws IOException {
        unitService.updateUnitsFromTable();
        return ResponseEntity.ok("Units updated successfully!");
    }
}
