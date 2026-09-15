package com.example.workreportplus.controller;

import com.example.workreportplus.dto.*;
import com.example.workreportplus.request.RegionReportRequest;
import com.example.workreportplus.request.searchparams.RegionReportSearchParams;
import com.example.workreportplus.response.DailyRegionReportResponse;
import com.example.workreportplus.response.ReportResponse;
import com.example.workreportplus.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/daily-work-report")
public class DailyWorkReportController {

    private static final Logger logger = LoggerFactory.getLogger(DailyWorkReportController.class);

    @Autowired private RegionReportService regionReportService;
    @Autowired private DescriptionTemplateService descriptionTemplateService;
    @Autowired private RegionService regionService;
    @Autowired private ContractorService contractorService;
    @Autowired private PlacesService placeService;
    @Autowired private GroupService groupService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private UserService userService;
    @Autowired private ShpsService shpsService;
    @Autowired private Status_30_100_Service status_30_100_service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DailyRegionReportResponse> submitReport(
            @RequestBody @Valid RegionReportRequest request,
            HttpServletRequest httpRequest) {

        DailyRegionReportResponse response = regionReportService.saveReport(request);

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID currentUserId = userService.getUserIdByEmail(currentEmail).orElse(null);
        auditLogService.log(
                "REPORT_SUBMIT",
                "daily-work-report",
                request.getRegionName() != null ? request.getRegionName() : "unknown",
                currentUserId,
                httpRequest.getRemoteAddr(),
                "Submitted daily region report : " + response.getId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/contractors")
    public Map<String, List<ContractorRelocation>> contractors(@RequestParam String date, UUID regionId) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate searchDate = LocalDate.parse(date, formatter);

        List<ContractorRelocation> arrived = shpsService.getArrivedContractors(searchDate,regionId );
        List<ContractorRelocation> departed = status_30_100_service.getDepartedContractors(searchDate, regionId);

        Map<String, List<ContractorDto>> arrivedMap = new HashMap<>();
        Map<String, List<ContractorDto>> departedMap = new HashMap<>();

        arrived.forEach(r -> arrivedMap.computeIfAbsent(r.getRegion().getId(), k -> new ArrayList<>()).add(r.getContractor()));
        departed.forEach(r -> departedMap.computeIfAbsent(r.getRegion().getId(), k -> new ArrayList<>()).add(r.getContractor()));

        return Map.of("arrived", arrived, "departed", departed);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<ReportResponse> viewRegionReport(@PathVariable String id) {
        ReportResponse report = regionReportService.getReportById(UUID.fromString(id));
        if (report == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchReports(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "region", required = false) String regionName,
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        LocalDate start = (startDate == null || startDate.isBlank()) ? today.withDayOfMonth(1) : LocalDate.parse(startDate, formatter);
        LocalDate end = (endDate == null || endDate.isBlank()) ? today : LocalDate.parse(endDate, formatter);

        RegionReportSearchParams params = new RegionReportSearchParams();
        params.setStartDate(start);
        params.setEndDate(end);
        params.setRegionName(regionName);
        params.setStatus(status);

        List<ReportResponse> reports = regionReportService.getReports(params);

        Map<String, Object> result = new HashMap<>();
        result.put("reports", reports);
        result.put("startDate", start.format(formatter));
        result.put("endDate", end.format(formatter));
        result.put("regionNames", regionService.getRegionNames());

        return ResponseEntity.ok(result);
    }
} // Adapted to work with Vue frontend using JSON endpoints
