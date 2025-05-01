package com.example.workreportplus.controller;

import com.example.workreportplus.dto.RegionDto;
import com.example.workreportplus.dto.Report100Record;
import com.example.workreportplus.service.RegionService;
import com.example.workreportplus.service.Report100Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api/report100")
public class Report100Controller {

    @Autowired
    private RegionService regionService;

    @Autowired
    private Report100Service report100Service;

    // Step 1 – Open initial UI
    @GetMapping
    public String loadReport100Page(Model model) {
        List<RegionDto> regions = regionService.getRegions();
        String defaultMonth = YearMonth.from(LocalDate.now()).toString();
        model.addAttribute("monthLabel", defaultMonth);

        model.addAttribute("regions", regions);
        return "report100";
    }

    @GetMapping("/generate")
    public String generateReport100Page(@RequestParam String regionName,
                                        @RequestParam String reportDate,
                                        Model model) {
        LocalDate parsedDate = LocalDate.parse(reportDate + "-01");
        int daysInMonth = YearMonth.from(parsedDate).lengthOfMonth();

        List<RegionDto> regions = regionService.getRegions();
        List<Report100Record> records = report100Service.getReportForRegionAndMonth(regionName, parsedDate);
        List<List<String>> result = report100Service.buildAttendanceTable(records, daysInMonth);

        List<Integer> days = IntStream.rangeClosed(1, daysInMonth).boxed().toList();

        model.addAttribute("regions", regions);
        model.addAttribute("selectedRegion", regionName);
        model.addAttribute("monthLabel", reportDate);
        model.addAttribute("daysInMonth", days);
        model.addAttribute("table", result); // new table matrix

        return "report100";
    }


    // Step 3 – Export report as Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(@RequestParam String regionName,
                                              @RequestParam @Valid LocalDate reportDate) {

        byte[] excelBytes = report100Service.exportToExcel(regionName, reportDate); // TODO: implement this method

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report100_" + regionName + "_" + reportDate + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

}
