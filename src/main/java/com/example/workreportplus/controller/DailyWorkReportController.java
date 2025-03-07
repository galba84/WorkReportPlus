package com.example.workreportplus.controller;

import com.example.workreportplus.request.RegionReportRequest;
import com.example.workreportplus.request.searchparams.RegionReportSearchParams;
import com.example.workreportplus.response.DailyRegionReportResponse;
import com.example.workreportplus.response.ReportResponse;
import com.example.workreportplus.service.RegionReportService;
import com.example.workreportplus.service.RegionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public String submitReport(@ModelAttribute @Valid RegionReportRequest request,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Validation failed. Please fill in all required fields.");
            return "new_report"; // Return back to form with error message
        }

        // Log submitted data (For now, we'll just log instead of storing it in DB)
        logger.info("Received Region Report: {}", request);
        if (request.getGroupReports() != null) {
            request.getGroupReports().forEach(group ->
                    logger.info("Group Report: {}", group));
        }
        DailyRegionReportResponse dailyRegionReportResponse = regionReportService.saveReport(request);
        model.addAttribute("successMessage", "Report submitted successfully!");
        model.addAttribute("regionReport", dailyRegionReportResponse);
        model.addAttribute(REGION_NAMES, regionService.getRegionNames());

        return "new_report"; // Redirect to the same page with a success message
    }

    @GetMapping
    public String showReportForm(Model model) {
        model.addAttribute("regionReport", new DailyRegionReportResponse());
        model.addAttribute(REGION_NAMES, regionService.getRegionNames());
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
    public String viewReport(@PathVariable String id, Model model) {
        ReportResponse report = regionReportService.getReportById(UUID.fromString(id));

        if (report == null) {
            model.addAttribute("errorMessage", "Report not found!");
            return "report_details";
        }

        model.addAttribute("report", report);
        return "daily-region-report";
    }


}
