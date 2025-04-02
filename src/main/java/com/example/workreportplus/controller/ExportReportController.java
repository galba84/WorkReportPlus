package com.example.workreportplus.controller;

import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.dto.RegionReportDto;
import com.example.workreportplus.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static java.time.LocalTime.now;

@Controller
@RequestMapping("/api/daily-work-report")
public class ExportReportController {

    public static final String REGION_REPORT = "Region Report";
    public static final String GROUP_REPORT = "Group Report";
    private final WordTemplateService wordTemplateService;
    private final RegionService regionService;
    private final GroupService groupService;
    private final GroupReportService groupReportService;
    private final RegionReportService regionReportService;
    private static final Set<String> reportTypes = Set.of(REGION_REPORT, GROUP_REPORT);

    public ExportReportController(WordTemplateService wordTemplateService, RegionService regionService,
                                  GroupService groupService, GroupReportService groupReportService,
                                  RegionReportService regionReportService
    ) {
        this.wordTemplateService = wordTemplateService;
        this.regionService = regionService;
        this.groupService = groupService;
        this.groupReportService = groupReportService;
        this.regionReportService = regionReportService;
    }

    @GetMapping("/export")
    public String showExportReportPage(Model model) {
        // List available templates
        List<String> templates = List.of(GROUP_REPORT, REGION_REPORT);
        List<String> regionNames = regionService.getRegionNames();
        List<String> groupNames = groupService.getGroupNames();

        // Add templates to the model for the dropdown selection
        model.addAttribute("templates", templates);
        model.addAttribute("regionNames", regionNames);
        model.addAttribute("groupNames", groupNames);

        return "exportReport"; // This will render exportReport.html
    }

    @GetMapping("/export/word")
    public ResponseEntity<byte[]> exportWord(@RequestParam String templateName,
                                             @RequestParam(required = false) String regionName,
                                             @RequestParam(required = false) String groupName,
                                             @RequestParam @Valid LocalDate reportDate

    ) throws IOException {

        if (!validateRequest(templateName, regionName, groupName, reportDate)) {
            return ResponseEntity.badRequest().build();
        }
        // Define placeholders and their values
        Map<String, String> variables = enreachTemplatevariables(templateName, regionName, groupName, reportDate);
        byte[] wordBytes = wordTemplateService.generateWordFromRtfTemplate(templateName, variables);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + templateName + now() + ".rtf")
                .contentType(MediaType.valueOf("application/rtf"))
                .body(wordBytes);
    }

    private boolean validateRequest(String templateName, String regionName, String groupName, LocalDate reportDate) {
        if (!StringUtils.hasText(templateName) || !reportTypes.contains(templateName)) {
            return false;
        }

        if (GROUP_REPORT.equalsIgnoreCase(templateName)) {
            if (!StringUtils.hasText(groupName) || !groupService.groupExistsByName(groupName)) {
                return false;
            }

        } else if (REGION_REPORT.equalsIgnoreCase(templateName)) {
            if (!StringUtils.hasText(regionName) || !regionService.regionExistsByName(regionName)) {
                return false;
            }

        }
        if (reportDate.isAfter(LocalDate.now().plusDays(1))) {
            return false;
        }
        return true;
    }

    private Map<String, String> enreachTemplatevariables(String templateName, String regionName, String groupName,
                                                         LocalDate reportDate) throws IOException {
        Map<String, String> variables = new HashMap<>();
        if (REGION_REPORT.equalsIgnoreCase(templateName)) {
            variables.put("regionName", regionName);
            variables.put("reportDate", reportDate.toString());
            UUID regionId = regionService.getRegionIdByName(regionName);
            List<UUID> groupIds = groupService.getGroupIdsByRegionId(regionId);
            List<GroupReportDto> groupReportDtoList = new ArrayList<>();
            for (UUID groupId : groupIds) {
                groupReportDtoList.add(groupReportService.getReportByGroupIdAndDate(groupId, reportDate));
            }
            List<String> groupReportsString = new ArrayList<>();
            for (GroupReportDto groupReportDto : groupReportDtoList) {
                Map<String, String> variablesForGroupReport = new HashMap<>();
                String groupNameLocal = groupService.getGroupNameById(groupReportDto.getGroupName());
                createGroupReport(groupNameLocal, reportDate, variablesForGroupReport);
                byte[] data = wordTemplateService.generateWordFromRtfTemplate(GROUP_REPORT, variablesForGroupReport);
                groupReportsString.add("\n");
                groupReportsString.add("___________________________________");
                groupReportsString.add("GROUP REPORT : " + groupReportDto.getGroupName());


                groupReportsString.add(new String(data, StandardCharsets.UTF_8));
                groupReportsString.add("___________________________________");
                groupReportsString.add("\n");

            }

            variables.put("groupReports", String.join("\n", groupReportsString));

            RegionReportDto report = regionReportService.getReportByRegionIdAndDate(regionId, reportDate);
            variables.put("regionReportDescription", report.getRegionDescription());
        } else if (GROUP_REPORT.equalsIgnoreCase(templateName)) {
            createGroupReport(groupName, reportDate, variables);
        }
        return variables;
    }


    private void createGroupReport(String groupName, LocalDate reportDate, Map<String, String> variables) {
        variables.put("groupName", groupName);
        variables.put("reportDate", reportDate.toString());
        UUID groupId = groupService.getGroupIdByName(groupName);
        GroupReportDto report = groupReportService.getReportByGroupIdAndDate(groupId, reportDate);
        variables.put("contractors", String.join("\n", report.getContractorsIds().toString()));
        variables.put("groupReportDescription", report.getDescription());
    }
}
