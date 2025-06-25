package com.example.workreportplus.controller;

import com.example.workreportplus.dto.*;
import com.example.workreportplus.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
    private final RegionReportTemplateService regionReportTemplateService;
    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ExportReportController(WordTemplateService wordTemplateService, RegionService regionService,
                                  GroupService groupService, GroupReportService groupReportService,
                                  RegionReportService regionReportService,
                                  DescriptionTemplateService descriptionTemplateService,
                                  ContractorService contractorService, PlacesService placesService, RegionReportTemplateService regionReportTemplateService) {
        this.wordTemplateService = wordTemplateService;
        this.regionService = regionService;
        this.groupService = groupService;
        this.groupReportService = groupReportService;
        this.regionReportService = regionReportService;
        this.descriptionTemplateService = descriptionTemplateService;
        this.contractorService = contractorService;
        this.placesService = placesService;
        this.regionReportTemplateService = regionReportTemplateService;
    }


    /**
     * Export all daily region reports for the given month.
     * URL: /api/daily-work-report/export/word/{month}?templateName=...&regionId=...
     * month in ISO-8601 Year-Month format: YYYY-MM
     */
    @GetMapping("/export/word/{month}")
    public ResponseEntity<byte[]> exportMonthlyWord(
            @PathVariable String month,
            @RequestParam String templateName,
            @RequestParam String regionId) throws IOException {
        // Parse YearMonth
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        // Validate region
        UUID regionUUID;
        try {
            regionUUID = UUID.fromString(regionId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        if (!regionService.regionExistsById(regionUUID)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Load most recent template for region
        RegionReportTemplateDto templateDto =
                regionReportTemplateService.getRecentTemplateByRegionId(regionUUID);

        ByteArrayOutputStream allReports = new ByteArrayOutputStream();

        // Iterate each day in month
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!regionReportService.existsByRegionIdAndDate(regionUUID, date)) continue;
            Map<String, String> vars = enrichTemplateVariables(regionService.getRegionNameById(regionUUID), date, templateDto);
            byte[] piece = wordTemplateService.generateWordFromRtfTemplate(
                    templateName,
                    vars,
                    templateDto);
            allReports.write(piece);
        }

        // Build filename
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        String filename = templateName.replace(" ", "_")
                + "_" + ym + "_" + timestamp + ".rtf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.valueOf("application/rtf"))
                .body(allReports.toByteArray());
    }


    @GetMapping("/export/word")
    public ResponseEntity<?> exportWord(@RequestParam String templateName,
                                        @RequestParam(required = false) String regionId,
                                        @RequestParam(required = false) String groupName,
                                        @RequestParam @Valid LocalDate reportDate) throws IOException {

        UUID regionIdByName = regionService.getRegionIdByName(regionId);
        if (null != regionIdByName) {
            regionId = regionIdByName.toString();
        }

        if (!validateRequest(templateName, regionId, groupName, reportDate)) {
            return ResponseEntity.badRequest().build();
        }


        UUID regionUUID = UUID.fromString(regionId);
        RegionReportTemplateDto recentTemplateByRegionId = regionReportTemplateService
                .getRecentTemplateByRegionId(regionUUID);
        String regionName = regionService.getRegionNameById(regionUUID);

        boolean isReportExist = regionReportService.existsByRegionIdAndDate(regionUUID, reportDate);

        if (!isReportExist) {
            // Якщо звіту немає — повертаємо 404 Not Found з повідомленням
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Немає звіту для обраної дати та регіону");
        }

        Map<String, String> variables = enrichTemplateVariables(regionName, reportDate, recentTemplateByRegionId);
        byte[] wordBytes = wordTemplateService.generateWordFromRtfTemplate(templateName, variables, recentTemplateByRegionId);
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));

        String filename = templateName.replace(" ", "_") + "_" + reportDate + "_" + timestamp + ".rtf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
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

    private Map<String, String> enrichTemplateVariables(String regionName, LocalDate reportDate,
                                                        RegionReportTemplateDto template) throws IOException {
        UUID regionIdByName = regionService.getRegionIdByName(regionName);

        UUID lastReportIdByDate = regionReportService.getLastReportIdByDate(reportDate, regionIdByName);
        Map<String, String> variables = new HashMap<>();
        variables.put("regionName", regionName);
        variables.put("reportDate", formatDate(reportDate));


        if (template != null) {
            variables.put("preamble", template.getPreamble());
            variables.put("signature", template.getSignature());
        } else {
            variables.put("preamble", "на виконання Бойового розпорядження Головнокомандуючого Збройних Сил України від 15.03.2023 №12365 та Бойових наказів командира військової частини А4124 від 05.10.2023 №275ДСК, від 03.01.2024 №2ДСК, від 14.10.2023 №286ДСК, від 12.12.2023 №351ДСК та від 21.06.2024 №290ДСК");

            variables.put("signature",
                    " \n" +
                    "Тимчасово виконуючий обов’язки командира зведеного загону військової частини А4124\n" +
                    "лейтенант                        _____________                     Максим КРАМАРОВ\n");

        }
        List<UUID> groupIds = groupService.getGroupIdsByRegionId(regionIdByName);
        List<GroupReportDto> groupReportDtoList = groupIds.stream()
                .map(gid -> groupReportService.getReportByGroupIdAndDate(gid, reportDate, lastReportIdByDate))
                .filter(Objects::nonNull)
                .toList();

        List<String> groupReportsString = new ArrayList<>();
        for (GroupReportDto groupReportDto : groupReportDtoList) {
            Map<String, String> variablesForGroupReport = new HashMap<>();
            String groupNameLocal = groupService.getGroupNameById(groupReportDto.getGroupName());
            enreachGroupReport(groupReportDto, groupNameLocal, reportDate, variablesForGroupReport);
            byte[] data = wordTemplateService.generateWordFromRtfTemplate(GROUP_REPORT, variablesForGroupReport, null);
            groupReportsString.add(new String(data, StandardCharsets.UTF_8));
        }

        variables.put("groupReports", String.join("\n", groupReportsString));


        RegionReportDto report = regionReportService.getReportByRegionIdAndDate(regionIdByName, reportDate);
        // Null-safe contractor lists
        List<ContractorDto> arrived = (report.getArrivedContractors() != null && !report.getArrivedContractors().isEmpty())
                ? contractorService.getContractorsByIds(report.getArrivedContractors())
                : List.of();

        List<ContractorDto> departed = (report.getDepartedContractors() != null && !report.getDepartedContractors().isEmpty())
                ? contractorService.getContractorsByIds(report.getDepartedContractors())
                : List.of();
        String arrivedOrderNumber = "";
        String departedOrderNumber = "";
        LocalDate arrivedOrderDate = null;
        LocalDate departedOrderDate = null;
        if (report.getExtraData() != null) {
            arrivedOrderNumber = (report.getExtraData().get("arrivedOrderNumber"));
            departedOrderNumber = (report.getExtraData().get("departedOrderNumber"));
            arrivedOrderDate = safeParseDate(report.getExtraData().get("arrivedOrderDate"));
            departedOrderDate = safeParseDate(report.getExtraData().get("departedOrderDate"));
        }
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
        List<PlaceDto> fightingPlaces = placesService.getPlacesByIds(dto.getFightingPlaces());
        List<PlaceDto> restPlaces = placesService.getPlacesByIds(dto.getRestPlaces());
        String ammoHeader = "Витрата боєприпасів та розхід засобів: " ;
        String restContractorsHeader = "та" + System.lineSeparator();
        String restPlacesHeader = "на ППД " + System.lineSeparator();

        vars.put("groupName", groupName);
        vars.put("groupDetails", details);
        if (dto.getAmmunition() != null && dto.getAmmunition().length() > 6) {
            vars.put("ammunition", ammoHeader + dto.getAmmunition());
        }
        vars.put("reportDate", formatDate(reportDate));
        vars.put("fightingPlaces", formatPlaces(fightingPlaces));


        UUID groupId = groupService.getGroupIdByName(groupName);
//        GroupReportDto report = groupReportService.getReportByGroupIdAndDate(groupId, reportDate, lastReportIdByDate);
        List<ContractorDto> fightingContractors = contractorService.getAllContractorByIds(dto.getFightingContractors());
        List<ContractorDto> restContractors = contractorService.getAllContractorByIds(dto.getRestContractors());
        vars.put("fightingContractors", printContractors(fightingContractors));
        if (!restContractors.isEmpty()) {
            vars.put("restContractors", restContractorsHeader + printContractors(restContractors));
        }
        if (!restContractors.isEmpty()) {
            vars.put("restPlaces", restPlacesHeader + formatPlaces(restPlaces));
        }
        vars.put("groupReportDescription", dto.getDescription());
        vars.put("successReport", dto.getSuccessReport());
    }

    private String printContractors(List<ContractorDto> contractors) {
        return contractors.stream()
                .map(e -> "* " + e.getC_rank().toLowerCase() + " " + e.getLastName() + " " + e.getFirstName() + " " + e.getMiddleName() + " <<" + e.getNickName() + ">>")
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
