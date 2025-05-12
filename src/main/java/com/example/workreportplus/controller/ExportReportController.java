package com.example.workreportplus.controller;

import com.example.workreportplus.dto.ContractorDto;
import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.dto.PlaceDto;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;

@Controller
@RequestMapping("/api/daily-work-report")
public class ExportReportController {

    public static final String REGION_REPORT = "Region Report";
    public static final String GROUP_REPORT = "Group Report";
    private final WordTemplateService wordTemplateService;
    private final DescriptionTemplateService descriptionTemplateService;
    private final RegionService regionService;
    private final GroupService groupService;
    private final GroupReportService groupReportService;
    private final RegionReportService regionReportService;
    private static final Set<String> reportTypes = Set.of(REGION_REPORT);
    private final ContractorService contractorService;
    private final PlacesService placesService;

    public ExportReportController(WordTemplateService wordTemplateService, RegionService regionService,
                                  GroupService groupService, GroupReportService groupReportService,
                                  RegionReportService regionReportService,
                                  DescriptionTemplateService descriptionTemplateService,
                                  ContractorService contractorService, PlacesService placesService) {
        this.wordTemplateService = wordTemplateService;
        this.regionService = regionService;
        this.groupService = groupService;
        this.groupReportService = groupReportService;
        this.regionReportService = regionReportService;
        this.descriptionTemplateService = descriptionTemplateService;
        this.contractorService = contractorService;
        this.placesService = placesService;
    }

    @GetMapping("/export")
    public String showExportReportPage(Model model) {
        // List available templates
        List<String> templates = List.of(REGION_REPORT);
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
        Map<String, String> variables = enrichTemplateVariables(regionName, reportDate);
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

         else if (REGION_REPORT.equalsIgnoreCase(templateName)) {
            if (!StringUtils.hasText(regionName) || !regionService.regionExistsByName(regionName)) {
                return false;
            }

        }
        return reportDate.isBefore(LocalDate.now().plusDays(1));
    }

    private Map<String, String> enrichTemplateVariables(String regionName,
                                                        LocalDate reportDate) throws IOException {
        UUID lastReportIdByDate = regionReportService.getLastReportIdByDate(reportDate);
        Map<String, String> variables = new HashMap<>();
            variables.put("regionName", regionName);
            variables.put("reportDate", formatDate(reportDate));
            variables.put("signature", "підпис              _______________          О.В. Овчаренко");
            UUID regionId = regionService.getRegionIdByName(regionName);
            List<UUID> groupIds = groupService.getGroupIdsByRegionId(regionId);
            List<GroupReportDto> groupReportDtoList = new ArrayList<>();
            for (UUID groupId : groupIds) {
                GroupReportDto dto = groupReportService.getReportByGroupIdAndDate(groupId, reportDate, lastReportIdByDate);
                if (dto != null) {
                    groupReportDtoList.add(dto);
                }
            }
            List<String> groupReportsString = new ArrayList<>();
            for (GroupReportDto groupReportDto : groupReportDtoList) {
                Map<String, String> variablesForGroupReport = new HashMap<>();
                String groupNameLocal = groupService.getGroupNameById(groupReportDto.getGroupName());
                createGroupReport(groupReportDto, groupNameLocal, reportDate, variablesForGroupReport);
                byte[] data = wordTemplateService.generateWordFromRtfTemplate(GROUP_REPORT, variablesForGroupReport);
                groupReportsString.add("\n");
                groupReportsString.add("___________________________________");


                groupReportsString.add(new String(data, StandardCharsets.UTF_8));
                groupReportsString.add("___________________________________");
                groupReportsString.add("\n");

            }

            variables.put("groupReports", String.join("\n", groupReportsString));

            RegionReportDto report = regionReportService.getReportByRegionIdAndDate(regionId, reportDate);
            variables.put("regionReportDescription", report.getRegionDescription());

        return variables;
    }

    private String formatDate(LocalDate reportDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'року'", new Locale("uk"));
        return reportDate.format(formatter);
    }


    private void createGroupReport(GroupReportDto groupReportDto,
                                   String groupName, LocalDate reportDate, Map<String, String> variables) {

        String detailsByGroupId = descriptionTemplateService.getDetailsByGroupId(groupReportDto.getGroupId());
        UUID lastReportIdByDate = regionReportService.getLastReportIdByDate(reportDate);
        List <PlaceDto> places = placesService.getPlaceByIds(groupReportDto.getPlaceIds());
        variables.put("groupName", groupName);
        variables.put("groupDetails", detailsByGroupId);
        variables.put("reportDate", formatDate(reportDate));
        variables.put("places", formatPlaces(places));
        UUID groupId = groupService.getGroupIdByName(groupName);
        GroupReportDto report = groupReportService.getReportByGroupIdAndDate(groupId, reportDate, lastReportIdByDate);
        List<ContractorDto> contractor = contractorService.getAllContractorByIds(report.getContractorsIds());
        variables.put("contractors",
                contractor.stream()
                        .map(e -> "* " + e.getFirstName()+ " " + e.getLastName() +" - "
                                + e.getC_rank() + " ("+e.getNickName()+")")
                        .collect(Collectors.joining("\n"))
        );
        variables.put("groupReportDescription", report.getDescription());
    }

    private String formatPlaces(List<PlaceDto> places) {
        if (places == null || places.isEmpty()) return "";

        return places.stream()
                .map(p -> "- " + p.getName() + (p.getDistrict() != null ? " (" + p.getDistrict() + ")" : ""))
                .collect(Collectors.joining("\n"));
    }

}
