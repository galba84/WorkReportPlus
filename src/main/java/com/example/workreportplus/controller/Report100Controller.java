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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/report100")
public class Report100Controller {

    @Autowired
    private RegionService regionService;

    @Autowired
    private Report100Service report100Service;

    // GET list of regions for dropdown
    @GetMapping("/regions")
    public List<RegionDto> getRegions() {
        return regionService.getRegions();
    }

    // Generate attendance table data as JSON
    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReportData(
            @RequestParam String regionName,
            @RequestParam String reportDate) {
        LocalDate parsedDate = LocalDate.parse(reportDate + "-01");
        int daysInMonth = YearMonth.from(parsedDate).lengthOfMonth();

        List<Report100Record> records = report100Service.getReportForRegionAndMonth(regionName, parsedDate);
        List<List<String>> table = report100Service.buildAttendanceTable(records, daysInMonth);
        List<Integer> days = IntStream.rangeClosed(1, daysInMonth).boxed().toList();

        Map<String,Object> response = new HashMap<>();
        response.put("table", table);
        response.put("daysInMonth", days);
        return ResponseEntity.ok(response);
    }

    // Export report as Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam String regionName,
            @RequestParam @Valid LocalDate reportDate) {

        byte[] excelBytes = report100Service.exportToExcel(regionName, reportDate);
        List<Report100Record> records = report100Service.getReportForRegionAndMonth(regionName, reportDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report100_" + regionName + "_" + reportDate + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
