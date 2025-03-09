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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/daily-work-report")
public class DailyWorkReportController {

    private static final Logger logger = LoggerFactory.getLogger(DailyWorkReportController.class);
    public static final String REGION_NAMES = "regionNames";

    @Autowired
    private RegionReportService regionReportService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private ContractorService contractorService;
    @Autowired
    private PlacesService placeService;
    @Autowired
    private GroupService groupService;

    @PostMapping
    public String submitReport(@ModelAttribute @Valid RegionReportRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
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
        return "redirect:/api/daily-work-report";
    }

    @GetMapping
    public String showReportForm(Model model) {
        model.addAttribute("regionReport", new DailyRegionReportResponse());
        List<RegionDto> regions = regionService.getRegions();
        List<GroupDto> groups = groupService.getAllGroups();
        List<ContractorDto> contractors = contractorService.getContractors();
        List<PlaceDto> places = placeService.getAllPlaces();
        //mock
        groups.forEach(e->e.setContractors(contractors));
        model.addAttribute("regions", regions);
        model.addAttribute("groups", groups);
        model.addAttribute("contractors", contractors);
        model.addAttribute("places", places);

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
