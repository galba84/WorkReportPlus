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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
@RequestMapping("/api/daily-work-report")
public class DailyWorkReportController {

    private static final Logger logger = LoggerFactory.getLogger(DailyWorkReportController.class);
    public static final String REGION_NAMES = "regionNames";

    @Autowired
    private RegionReportService regionReportService;
    @Autowired
    private DescriptionTemplateService descriptionTemplateService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private ContractorService contractorService;
    @Autowired
    private PlacesService placeService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private ShpsService shpsService;

    @PostMapping
    public String submitReport(@ModelAttribute @Valid RegionReportRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest httpRequest
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed. Errors: "
                    + bindingResult.getAllErrors());
            return "redirect:/api/daily-work-report";
        }
        logger.info("Received Region Report: {}", request);
        if (request.getGroupReports() != null) {
            request.getGroupReports().forEach(group -> logger.info("Group Report: {}", group));
        }

        DailyRegionReportResponse dailyRegionReportResponse = regionReportService.saveReport(request);

        redirectAttributes.addFlashAttribute("successMessage", "Report submitted successfully for : " + dailyRegionReportResponse.getDate() + " id:" + dailyRegionReportResponse.getId());
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID currentUserId = userService.getUserIdByEmail(currentEmail).orElse(null);
        auditLogService.log(
                "REPORT_SUBMIT",
                "daily-work-report",
                request.getRegionName() != null ? request.getRegionName() : "unknown",
                currentUserId,
                httpRequest.getRemoteAddr(),
                "Submitted daily region report"
        );

        return "redirect:/api/daily-work-report";
    }

    @GetMapping("/contractors")
    @ResponseBody
    public Map<String, Map<String, List<ContractorDto>>> contractors(@RequestParam String date) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate searchDate = LocalDate.parse(date, formatter);

        List<ContractorRelocation> arrivedContractors = shpsService.getArrivedContractors(searchDate);
        List<ContractorRelocation> departedContractors = shpsService.getDeparturedContractors(searchDate);
        List<ContractorDto> contractorDtos = contractorService.getContractors();
        Map<String, List<ContractorDto>> arrivedMap = new HashMap<>();
        Map<String, List<ContractorDto>> departedMap = new HashMap<>();
        Map<String, List<ContractorDto>> contractors = new HashMap<>();
        contractors.put("all", contractorDtos);
        for (ContractorRelocation relocation : arrivedContractors) {
            String regionId = relocation.getRegion().getId();
            arrivedMap.computeIfAbsent(regionId, k -> new ArrayList<>()).add(relocation.getContractor());
        }

        for (ContractorRelocation relocation : departedContractors) {
            String regionId = relocation.getRegion().getId();
            departedMap.computeIfAbsent(regionId, k -> new ArrayList<>()).add(relocation.getContractor());
        }

        return Map.of(
                "arrived", arrivedMap,
                "departed", departedMap,
                "contractorsAll", contractors
        );
    }



    @GetMapping
    public String showReportForm(Model model) {
        model.addAttribute("regionReportRequest", new RegionReportRequest());
        List<RegionDto> regions = regionService.getRegions();
        List<GroupDto> groups = groupService.getAllGroups();

        Map<UUID, List<ContractorDto>> groupIdToContractorsMap = new HashMap<>();
        Map<UUID, String> groupIdToDescriptionMap = new HashMap<>();

        for (GroupDto group : groups) {
            UUID groupId = UUID.fromString(group.getId());

            // Fetch and assign contractors and descriptions
            groupIdToContractorsMap.put(groupId, contractorService.getContractorsByGroupId(groupId));
            groupIdToDescriptionMap.put(groupId, descriptionTemplateService.getContentByGroupId(groupId));
        }

        // Populate group fields directly
        for (GroupDto group : groups) {
            UUID groupId = UUID.fromString(group.getId());
            group.setContractors(groupIdToContractorsMap.get(groupId));
            group.setDefaultDescription(groupIdToDescriptionMap.get(groupId));

            // ✅ Fetch places using the regionId from the group itself
            group.setPlaces(placeService.getFightingPlaceByRegionId(group.getRegionId()));
        }

        model.addAttribute("regions", regions);
        model.addAttribute("groups", groups);

        return "new_report";
    }

    @GetMapping("/search")
    public String searchReports(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate",   required = false) String endDate,
            @RequestParam(value = "region",    required = false) String regionName,
            @RequestParam(value = "status",    required = false, defaultValue = "ACTIVE") String status,
            Model model) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        // Default start = first day of this month, end = today
        LocalDate startLocalDate = (startDate == null || startDate.isBlank())
                ? today.withDayOfMonth(1)
                : LocalDate.parse(startDate, formatter);

        LocalDate endLocalDate = (endDate == null || endDate.isBlank())
                ? today
                : LocalDate.parse(endDate, formatter);

        RegionReportSearchParams searchParams = new RegionReportSearchParams();
        searchParams.setStartDate(startLocalDate);
        searchParams.setEndDate(endLocalDate);
        searchParams.setRegionName(regionName);
        searchParams.setStatus(status);

        List<ReportResponse> reports = regionReportService.getReports(searchParams);

        model.addAttribute("reports", reports);
        model.addAttribute(REGION_NAMES, regionService.getRegionNames());
        model.addAttribute("startDate", startLocalDate.format(formatter));
        model.addAttribute("endDate",   endLocalDate.format(formatter));

        return "search_reports";
    }

    /**
     * Helper method to parse String to LocalDate safely.
     */
    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        try {
            return (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr, formatter) : null;
        } catch (DateTimeParseException e) {
            // Log the error and return null (or handle it differently if needed)
            logger.error("Invalid date format: " + dateStr);
            return null;
        }
    }


    @GetMapping("/view/{id}") // ✅ Fixed PathVariable Mapping
    public String viewRegionReport(@PathVariable String id, Model model) {
        ReportResponse report = regionReportService.getReportById(UUID.fromString(id));

        if (report == null) {
            model.addAttribute("errorMessage", "Report not found!");
            return "report_details";
        }

        model.addAttribute("report", report);
        return "daily-region-report";
    }


}
