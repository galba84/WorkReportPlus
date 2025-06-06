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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
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
    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

    @GetMapping("/export/word")
    public ResponseEntity<byte[]> exportWord(@RequestParam String templateName,
                                             @RequestParam(required = false) String regionId,
                                             @RequestParam(required = false) String groupName,
                                             @RequestParam @Valid LocalDate reportDate) throws IOException {

        if (!validateRequest(templateName, regionId, groupName, reportDate)) {
            return ResponseEntity.badRequest().build();
        }

        UUID regionUUID = UUID.fromString(regionId);
        String regionName = regionService.getRegionNameById(regionUUID);

        Map<String, String> variables = enrichTemplateVariables(regionName, reportDate);
        byte[] wordBytes = wordTemplateService.generateWordFromRtfTemplate(templateName, variables);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + templateName.replace(" ", "_") + "_" + reportDate + ".rtf")
                .contentType(MediaType.valueOf("application/rtf"))
                .body(wordBytes);
    }

    private boolean validateRequest(String templateName, String regionId, String groupName, LocalDate reportDate) {
        if (!StringUtils.hasText(templateName) || !reportTypes.contains(templateName)) {
            return false;
        } else if (REGION_REPORT.equalsIgnoreCase(templateName)) {
            if (!StringUtils.hasText(regionId)) return false;
            try {
                UUID regionUUID = UUID.fromString(regionId);
                return regionService.regionExistsById(regionUUID) && reportDate.isBefore(LocalDate.now().plusDays(1));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> enrichTemplateVariables(String regionName, LocalDate reportDate) throws IOException {
        UUID regionIdByName = regionService.getRegionIdByName(regionName);
        UUID lastReportIdByDate = regionReportService.getLastReportIdByDate(reportDate, regionIdByName);
        Map<String, String> variables = new HashMap<>();
        variables.put("regionName", regionName);
        variables.put("reportDate", formatDate(reportDate));

        variables.put("preamble", "на виконання Бойового розпорядження Головнокомандуючого Збройних Сил України від 15.03.2023 №12365 та Бойових наказів командира військової частини А4124 від 05.10.2023 №275ДСК, від 03.01.2024 №2ДСК, від 14.10.2023 №286ДСК, від 12.12.2023 №351ДСК та від 21.06.2024 №290ДСК");

        variables.put("signature", " \n" +
                " \t\n" +
                "Тимчасово виконуючий обов’язки командира зведеного загону військової частини А4124\n" +
                "лейтенант                                       _____________                     Максим КРАМАРОВ\n");

        UUID regionId = regionService.getRegionIdByName(regionName);
        List<UUID> groupIds = groupService.getGroupIdsByRegionId(regionId);
        List<GroupReportDto> groupReportDtoList = groupIds.stream()
                .map(gid -> groupReportService.getReportByGroupIdAndDate(gid, reportDate, lastReportIdByDate))
                .filter(Objects::nonNull)
                .toList();

        List<String> groupReportsString = new ArrayList<>();
        for (GroupReportDto groupReportDto : groupReportDtoList) {
            Map<String, String> variablesForGroupReport = new HashMap<>();
            String groupNameLocal = groupService.getGroupNameById(groupReportDto.getGroupName());
            enreachGroupReport(groupReportDto, groupNameLocal, reportDate, variablesForGroupReport);
            byte[] data = wordTemplateService.generateWordFromRtfTemplate(GROUP_REPORT, variablesForGroupReport);
            groupReportsString.add(new String(data, StandardCharsets.UTF_8));
//            groupReportsString.add("___________________________________\n");
        }

        variables.put("groupReports", String.join("\n", groupReportsString));


        RegionReportDto report = regionReportService.getReportByRegionIdAndDate(regionId, reportDate);
        List<ContractorDto> arrived = contractorService.getContractorsByIds(report.getArrivedContractors());
        List<ContractorDto> departed = contractorService.getContractorsByIds(report.getDepartedContractors());

        String arrivedOrderNumber = (report.getExtraData().get("arrivedOrderNumber"));
        String departedOrderNumber = (report.getExtraData().get("departedOrderNumber"));
        LocalDate arrivedOrderDate = safeParseDate(report.getExtraData().get("arrivedOrderDate"));
        LocalDate departedOrderDate = safeParseDate(report.getExtraData().get("departedOrderDate"));
        variables.put("arrivedOrder", "№" + arrivedOrderNumber + " від " + formatDate(arrivedOrderDate));
        variables.put("departedOrder", "№" + departedOrderNumber + " від " + formatDate(departedOrderDate));

        variables.put("arrivedContractors", printContractors(arrived));
        variables.put("departedContractors", printContractors(departed));
        variables.put("regionReportDescription", report.getRegionDescription());

        return variables;
    }



    private LocalDate safeParseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        try {
            // Try dd.MM.yyyy first
            return LocalDate.parse(dateStr, CUSTOM_FORMATTER);
        } catch (DateTimeParseException e) {
            // Fallback to ISO yyyy-MM-dd
            try {
                return LocalDate.parse(dateStr); // uses ISO_LOCAL_DATE by default
            } catch (DateTimeParseException ex) {
                return null; // or log warning
            }
        }
    }

    private void enreachGroupReport(GroupReportDto dto, String groupName, LocalDate reportDate, Map<String, String> vars) {
        String details = descriptionTemplateService.getDetailsByGroupId(dto.getGroupId());
        UUID regionIdByName = regionService.getRegionIdByName(dto.getRegionName());
        UUID lastReportIdByDate = regionReportService.getLastReportIdByDate(reportDate, regionIdByName);
        List<PlaceDto> places = placesService.getPlacesByIds(dto.getFightingPlaces());
        String ammoHeader = "Витрата боєприпасів та розхід засобів:" + System.lineSeparator();

        vars.put("groupName", groupName);
        vars.put("groupDetails", details);
        if (dto.getAmmunition() != null && dto.getAmmunition().length() > 2) {
            vars.put("ammunition", ammoHeader + dto.getAmmunition());
        }
        vars.put("reportDate", formatDate(reportDate));
        vars.put("fightingPlaces", formatPlaces(places));

        UUID groupId = groupService.getGroupIdByName(groupName);
        GroupReportDto report = groupReportService.getReportByGroupIdAndDate(groupId, reportDate, lastReportIdByDate);
        List<ContractorDto> contractors = contractorService.getAllContractorByIds(report.getFightingContractors());
        vars.put("fightingContractors", printContractors(contractors));
        vars.put("groupReportDescription", report.getDescription());
        vars.put("successReport", report.getSuccessReport());
    }

    private String printContractors(List<ContractorDto> contractors) {
        return contractors.stream()
                .map(e -> "* " +  e.getC_rank().toLowerCase() + " " + e.getLastName() + " " + e.getFirstName() + " " + e.getMiddleName()  + " <<" + e.getNickName() + ">>")
                .collect(Collectors.joining("\n"));
    }

    private String formatPlaces(List<PlaceDto> places) {
        if (places == null || places.isEmpty()) return "";
        return places.stream()
                .map(p -> "- " + p.getName() + (p.getDistrict() != null ? " (" + p.getDistrict() + ")" : ""))
                .collect(Collectors.joining("\n"));
    }

    private String formatDate(LocalDate reportDate) {
        if (reportDate == null) {
            return ""; // or return "—" or any fallback text
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'року'", new Locale("uk"));
        return reportDate.format(formatter);
    }

}
