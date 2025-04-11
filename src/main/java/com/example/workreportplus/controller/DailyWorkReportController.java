package com.example.workreportplus.controller;

import com.example.workreportplus.dto.ContractorDto;
import com.example.workreportplus.dto.GroupDto;
import com.example.workreportplus.dto.PlaceDto;
import com.example.workreportplus.dto.RegionDto;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @PostMapping
    public String submitReport(@ModelAttribute @Valid RegionReportRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed.");
            return "redirect:/new_report";
        }

        logger.info("Received Region Report: {}", request);
        if (request.getGroupReports() != null) {
            request.getGroupReports().forEach(group -> logger.info("Group Report: {}", group));
        }

        regionReportService.saveReport(request);

        redirectAttributes.addFlashAttribute("successMessage", "Report submitted successfully!");
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

    @GetMapping
    public String showReportForm(Model model) {
        model.addAttribute("regionReport", new DailyRegionReportResponse());
        List<RegionDto> regions = regionService.getRegions();
        List<GroupDto> groups = groupService.getAllGroups();
        Map<UUID,List<ContractorDto>> groupIdToContractorsMap = new HashMap<>();
        Map<UUID,List<PlaceDto>> groupIdToPlacesMap = new HashMap<>();
        Map<UUID,String> groupIdToDescriptionMap = new HashMap<>();

        groupService.getAllGroupIds().forEach(groupId -> {
            groupIdToContractorsMap.put(groupId, contractorService.getContractorsByGroupId(groupId));
            groupIdToDescriptionMap.put(groupId, descriptionTemplateService.getContentByGroupId(groupId));
            groupIdToPlacesMap.put(groupId, placeService.getPlaceByRegionId(groupService.getRegionIdByGroupId(groupId)));
        });

        groups.forEach(e-> e.setContractors(
                groupIdToContractorsMap.get(UUID.fromString(e.getId()))));
        groups.forEach(e-> e.setDefaultDescription(
                groupIdToDescriptionMap.get(UUID.fromString(e.getId()))));
        groups.forEach(e-> e.setPlaces(
                groupIdToPlacesMap.get(UUID.fromString(e.getId()))));

        model.addAttribute("regions", regions);
        model.addAttribute("groups", groups);

        return "new_report";
    }

    @GetMapping("/search")
    public String searchReports(@RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "region", required = false) String regionName,
                                Model model) {
        // Date formatter (adjust format based on your input format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convert Strings to LocalDate safely
        LocalDate startLocalDate = parseDate(startDate, formatter);
        LocalDate endLocalDate = parseDate(endDate, formatter);

        // Create and populate search parameters
        RegionReportSearchParams searchParams = new RegionReportSearchParams();
        searchParams.setStartDate(startLocalDate);
        searchParams.setEndDate(endLocalDate);
        searchParams.setRegionName(regionName);

        // Fetch reports with filters applied
        List<ReportResponse> reports = regionReportService.getReports(searchParams);

        // Add to the model for display in the template
        model.addAttribute("reports", reports);
        model.addAttribute(REGION_NAMES, regionService.getRegionNames());

        return "search_reports"; // Returns the same template with search results
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
