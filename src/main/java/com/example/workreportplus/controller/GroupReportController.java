/**
 * @author Alex Sereda
 * @date 23.05.2025 12:33
 */
package com.example.workreportplus.controller;

import com.example.workreportplus.dto.GroupReportPrefillDto;
import com.example.workreportplus.service.GroupReportService;
import com.example.workreportplus.service.GroupService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class GroupReportController {

    private final GroupService groupService;
    private final GroupReportService groupReportService;

    public GroupReportController(GroupService groupService, GroupReportService groupReportService) {
        this.groupService = groupService;
        this.groupReportService = groupReportService;
    }

    @GetMapping("/api/group-report-prefill")
    public List<GroupReportPrefillDto> getGroupReportPrefill(
            @RequestParam UUID regionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {
        return groupReportService.getPrefilledGroupReports(regionId, date);
    }

}
