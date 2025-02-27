package com.example.workreportplus.controller;

import com.example.workreportplus.dto.RegionReportDto;
import com.example.workreportplus.response.DailyRegionReportResponse;
import com.example.workreportplus.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/api/daily-work-report")
public class DailyWorkReportController {

    private static final Logger logger = LoggerFactory.getLogger(DailyWorkReportController.class);

    @Autowired
    private ReportService service;

    @PostMapping
    public String submitReport(@ModelAttribute @Valid RegionReportDto regionReportDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Validation failed. Please fill in all required fields.");
            return "new_report"; // Return back to form with error message
        }

        // Log submitted data (For now, we'll just log instead of storing it in DB)
        logger.info("Received Region Report: {}", regionReportDto);
        if (regionReportDto.getGroupReports() != null) {
            regionReportDto.getGroupReports().forEach(group ->
                    logger.info("Group Report: {}", group));
        }

        model.addAttribute("successMessage", "Report submitted successfully!");
        return "new_report"; // Redirect to the same page with a success message
    }

    @GetMapping
    public String showReportForm(Model model) {
        model.addAttribute("regionReport", new RegionReportDto());
        return "new_report";
    }

    @GetMapping("/search")
    public String searchReports(@RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "region", required = false) String region,
                                Model model) {
        model.addAttribute("reports", getResponseStub());
        return "search_reports"; // Returns the same template with search results
    }

    private static List<DailyRegionReportResponse> getResponseStub() {
        return ReportService.generateSampleReports();
    }

    @GetMapping("/view/{id}") // ✅ Fixed PathVariable Mapping
    public String viewReport(@PathVariable String id, Model model) {
        List<DailyRegionReportResponse> reports = getResponseStub();
        DailyRegionReportResponse report = reports.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (report == null) {
            model.addAttribute("errorMessage", "Report not found!");
            return "report_details";
        }

        model.addAttribute("report", report);
        return "daily-region-report";
    }


}
