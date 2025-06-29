package com.example.workreportplus.controller;

import com.example.workreportplus.dto.AppSettingDto;
import com.example.workreportplus.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;

import static com.example.workreportplus.controller.AppSettingsController.VOCABULARY_UPDATE_JOB;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // ✅ applies to all methods in the controller
public class AdminController {

    public static final String SYSTEM_USER = "00000000-0000-0000-0000-000000000000";
    private final ContractorService contractorService;
    private final GroupService groupService;
    private final RegionService regionService;
    private final DescriptionTemplateService descriptionTemplateService;
    private final PlacesService placesService;
    private final RankService rankService;
    private final PositionService positionService;
    private final UnitService unitService;
    private final AuditLogService auditLogService;
    private final AppSettingsService appSettingsService;

    public AdminController(ContractorService contractorService, GroupService groupService,
                           RegionService regionService, DescriptionTemplateService descriptionTemplateService,
                           PlacesService placesService, RankService rankService, PositionService positionService,
                           UnitService unitService, AuditLogService auditLogService, AppSettingsService appSettingsService) {
        this.contractorService = contractorService;
        this.groupService = groupService;
        this.regionService = regionService;
        this.descriptionTemplateService = descriptionTemplateService;
        this.placesService = placesService;
        this.rankService = rankService;
        this.positionService = positionService;
        this.unitService = unitService;
        this.auditLogService = auditLogService;
        this.appSettingsService = appSettingsService;
    }


    @PostMapping("/regions")
    public ResponseEntity<String> updateRegionsFromTable() throws IOException {
        regionService.updateRegionsFromTable();
        return ResponseEntity.ok("Regions updated successfully!");
    }

    @PostMapping("/groups")
    public ResponseEntity<String> updateGroupsFromTable() throws IOException {
        groupService.updateGroupsFromTable();
        return ResponseEntity.ok("Groups updated successfully!");
    }

    @PostMapping("/contractors")
    public ResponseEntity<String> updateContractorsFromTable() throws IOException {
        contractorService.updateContractorsFromTable();
        return ResponseEntity.ok("Contractors updated successfully!");
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

    @PostMapping("/sync")
    public ResponseEntity<String> syncAll() throws IOException {
        syncAllAdminData();
        return ResponseEntity.ok("SyncAll!");
    }

    @Scheduled(cron = "0 0 */2 * * *") // Every 2 hours at minute 0
    public void syncAllAdminData() throws UnknownHostException {
        try {
            boolean run = false;
            Optional<AppSettingDto> key = appSettingsService.findByKey(VOCABULARY_UPDATE_JOB);
            if (key.isPresent()) {
                run = Boolean.parseBoolean(key.get().getSettingValue());
            }
            if (run) {
                auditLogService.log(
                        "syncAllAdminData",
                        this.getClass().getName(),
                        "unknown",
                        UUID.fromString(SYSTEM_USER),
                        InetAddress.getLocalHost().getHostAddress(),
                        "Start sync All Admin Data"
                );

                regionService.updateRegionsFromTable();

                groupService.updateGroupsFromTable();

                contractorService.updateContractorsFromTable();

                descriptionTemplateService.updateFromTableSource();

                placesService.updatePlacesFromTable();

                rankService.updateRanksFromTable();

                positionService.updatePositionsFromTable();

                unitService.updateUnitsFromTable();
            } else {
                auditLogService.log(
                        "syncAllAdminData run false",
                        this.getClass().getName(),
                        "unknown",
                        UUID.fromString(SYSTEM_USER),
                        InetAddress.getLocalHost().getHostAddress(),
                        "Start sync All Admin Data"
                );
            }
        } catch (Exception e) {
            auditLogService.log(
                    "syncAllAdminData update failed",
                    this.getClass().getName(),
                    "unknown",
                    UUID.fromString(SYSTEM_USER),
                    InetAddress.getLocalHost().getHostAddress(),
                    "syncAllAdminData update failed"
            );
        }
    }
}
