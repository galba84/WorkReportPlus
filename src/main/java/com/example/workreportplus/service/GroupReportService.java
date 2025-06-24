package com.example.workreportplus.service;


import com.example.jooq.tables.records.GroupreportRecord;
import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.ENUM.ServiceStatus;
import com.example.workreportplus.dto.*;
import com.example.workreportplus.exception.DatabaseAccessException;
import com.example.workreportplus.exception.ReportNotFoundException;
import com.example.workreportplus.mapper.GroupReportMapper;
import com.example.workreportplus.mapper.RegionReportMapper;
import com.example.workreportplus.request.searchparams.GroupReportSearchParams;
import com.example.workreportplus.request.searchparams.SearchParams;
import com.example.workreportplus.response.ContractorResponse;
import com.example.workreportplus.response.DailyGroupReportResponse;
import com.example.workreportplus.response.ReportResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.jooq.tables.Groupreport.GROUPREPORT;

@Service
public class GroupReportService implements ReportService {
    private final DSLContext dsl;
    private final GroupReportMapper groupReportMapper;
    private final ContractorService contractorService;
    private final PositionService positionService;
    private final PlacesService placeService;
    private final GroupService groupService;
    private final OperativeReportService operativeReportService;
    private final RegionService regionService;
    private final Status_30_100_Service status_30_100_service;
    private final DescriptionTemplateService descriptionTemplateService;
    ObjectMapper objectMapper = new ObjectMapper();

    public GroupReportService(DSLContext dsl, RegionReportMapper regionReportMapper,
                              GroupReportMapper groupReportMapper, ContractorService contractorService,
                              PositionService positionService, PlacesService placeService,
                              GroupService groupService, OperativeReportService operativeReportService, RegionService regionService, Status_30_100_Service status30100Service, DescriptionTemplateService descriptionTemplateService) {
        this.dsl = dsl;
        this.groupReportMapper = groupReportMapper;
        this.contractorService = contractorService;
        this.positionService = positionService;
        this.placeService = placeService;
        this.groupService = groupService;
        this.operativeReportService = operativeReportService;
        this.regionService = regionService;
        status_30_100_service = status30100Service;
        this.descriptionTemplateService = descriptionTemplateService;
    }


    @Override
    public List<ReportResponse> getReports(SearchParams sp) {
        GroupReportSearchParams searchParams = (GroupReportSearchParams) sp;

        try {
            // Ensure groupId is a valid integer
            UUID groupId = UUID.fromString(searchParams.getGroupId());

            // Ensure dates are valid
            LocalDate startDate = searchParams.getStartDate();
            LocalDate endDate = searchParams.getEndDate();

            // Validate date range
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new RuntimeException("Start date cannot be after end date");
            }

            // Fetch reports
            Condition condition = GROUPREPORT.GROUP_ID.eq(groupId);
            if (startDate != null) {
                condition = condition.and(GROUPREPORT.REPORT_DATE.ge(startDate));
            }
            if (endDate != null) {
                condition = condition.and(GROUPREPORT.REPORT_DATE.le(endDate));
            }

            Result<GroupreportRecord> records = dsl.selectFrom(GROUPREPORT)
                    .where(condition)
                    .fetch();

            // Convert records to ReportResponse objects
            return records.stream()

                    .map(record ->
                            {
                                Boolean statusRaw = record.get(GROUPREPORT.STATUS);
                                ReportStatus status = ReportStatus.ACTIVE;
                                if (statusRaw != null) {
                                    status = ReportStatus.fromBoolean(statusRaw);
                                }


                                return DailyGroupReportResponse.builder()
                                        .regionReportId(record.get(GROUPREPORT.ID))
                                        .description(record.get(GROUPREPORT.DESCRIPTION))
                                        .contractors(getContractorResponses(record))
                                        .status(status)
                                        .build();
                            }
                    )

                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            // Handle invalid group ID
            throw new DatabaseAccessException("Invalid group ID: " + searchParams.getGroupId(), e);
        } catch (DataAccessException e) {
            // Handle database access exceptions
            throw new DatabaseAccessException("Failed to fetch reports", e);
        } catch (NullPointerException e) {
            // Handle null pointer exceptions (e.g., if startDate or endDate is null)
            throw new DatabaseAccessException("Start or end date is null", e);
        }
    }

    public List<DailyGroupReportResponse> getReportsByRegionId(UUID regionReportId) {
        try {
            // Fetch reports
            Condition condition = GROUPREPORT.REGION_REPORT_ID.eq(regionReportId);
            condition = condition.and(GROUPREPORT.STATUS).eq(Boolean.TRUE);
            Result<GroupreportRecord> records = dsl.selectFrom(GROUPREPORT)
                    .where(condition)
                    .fetch();
            // Convert records to ReportResponse objects
            return records.stream()
                    .map(this::mapRecordToResponse)
                    .toList();
        } catch (NumberFormatException e) {
            // Handle invalid group ID
            throw new DatabaseAccessException("Invalid region report ID: " + regionReportId, e);
        } catch (DataAccessException e) {
            // Handle database access exceptions
            throw new DatabaseAccessException("Failed to fetch reports", e);
        } catch (NullPointerException e) {
            // Handle null pointer exceptions (e.g., if startDate or endDate is null)
            throw new DatabaseAccessException("Start or end date is null", e);
        }
    }

    private List<ContractorResponse> getContractorResponses(GroupreportRecord record) {
        UUID[] fightingIds = record.get(GROUPREPORT.FIGHTING_CONTRACTORS);
        UUID[] restIds     = record.get(GROUPREPORT.REST_CONTRACTORS);

        // Merge arrays, filter out nulls, flatten and dedupe
        List<UUID> allIds = Stream.of(fightingIds, restIds)
                .filter(arr -> arr != null)
                .flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());

        if (allIds.isEmpty()) {
            return List.of();
        }

        return allIds.stream()
                .map(contractorService::getContractorById)
                .map(e -> ContractorResponse.builder()
                        .firstName(e.getFirstName())
                        .lastName(e.getLastName())
                        .rank(e.getC_rank())
                        .position(positionService.getNameById(e.getPositionId()))
                        .nickname(e.getNickName())
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    public ReportResponse getReportById(UUID id) {
        try {
            DailyGroupReportResponse record = dsl.selectFrom(GROUPREPORT)
                    .where(GROUPREPORT.ID.eq(id))
                    .fetchOneInto(DailyGroupReportResponse.class); // Use fetchOneInto() for a single result
            if (record == null) {
                throw new ReportNotFoundException("Report with ID " + id + " not found");
            }
            return record; // Assuming DailyGroupReportResponse extends ReportResponse or is compatible
        } catch (DataAccessException e) {
            // Handle database access exceptions
            throw new DatabaseAccessException("Failed to fetch report with ID " + id, e);
        }
    }

    @Override
    public List<UUID> getReportIdsByRegionAndPeriod(UUID regionId, LocalDate fromDate, LocalDate toDate) {
        return List.of();
    }

    private DailyGroupReportResponse mapRecordToResponse(GroupreportRecord record) {

        Boolean b = record.get(GROUPREPORT.STATUS);
        ReportStatus status = ReportStatus.ACTIVE;
        if (b != null) {
            status = ReportStatus.fromBoolean(b);
        }

        // 🔹 Working Areas
        List<PlaceDto> workingAreas = List.of();
        UUID[] placeIds = record.get(GROUPREPORT.FIGHTING_PLACES);
        if (placeIds != null && placeIds.length > 0) {
            workingAreas = placeService.getPlacesByIds(List.of(placeIds));
        }

        // 🔹 Ammunition
        List<AmmunitionDto> parsedAmmo = List.of();
        String ammunitionJson = String.valueOf(record.get(GROUPREPORT.AMMUNITION));
        if (ammunitionJson != null && !ammunitionJson.isBlank()) {
            try {
                parsedAmmo = objectMapper.readValue(ammunitionJson, new TypeReference<>() {
                });
            } catch (Exception ignored) {
            }
        }

        return DailyGroupReportResponse.builder()
                .id(record.getId())
                .groupName(groupService.getGroupNameById(record.get(GROUPREPORT.GROUP_ID).toString()))
                .regionReportId(record.get(GROUPREPORT.REGION_REPORT_ID))
                .date(record.get(GROUPREPORT.REPORT_DATE))
                .description(record.get(GROUPREPORT.DESCRIPTION))
                .status(status)
                .worked(record.get(GROUPREPORT.WORKED))
                .contractors(getContractorResponses(record))
                .extraData(getExtraDataGroupReport(record))
                .contractorLooses(getContractorLooses(record))
                .workingAreas(workingAreas)
                .ammunition(parsedAmmo) // ✅ pass parsed list
                .createdBy(record.get(GROUPREPORT.CREATED_BY))
                .createdOn(record.get(GROUPREPORT.CREATED_ON).toLocalDate())
                .updatedBy(record.get(GROUPREPORT.UPDATED_BY))
                .updatedOn(record.get(GROUPREPORT.UPDATED_ON).toLocalDate())
                .build();
    }


    private static List<UUID> getContractorLooses(GroupreportRecord record) {
        UUID[] elements = record.get(GROUPREPORT.LOOSES);
        if (elements == null) {
            return List.of();
        }
        return List.of(elements);
    }


    public Map<String, String> getExtraDataGroupReport(GroupreportRecord record) {
        JSONB jsonb = record.get(GROUPREPORT.EXTRA_DATA_GROUP_REPORT);
        if (jsonb == null) {
            return Collections.emptyMap();
        }

        try {
            Map<String, List<String>> raw = objectMapper.readValue(
                    jsonb.data(), new TypeReference<>() {
                    }
            );

            return raw.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> String.join(",", e.getValue())
                    ));
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSONB to Map", e);
        }
    }


    public GroupReportDto getReportByGroupIdAndDate(UUID groupId, LocalDate reportDate, UUID regionReportId) {
        Condition condition = GROUPREPORT.GROUP_ID.eq(groupId);
        if (reportDate != null) {
            condition = condition.and(GROUPREPORT.REPORT_DATE.eq(reportDate));
        }
        if (regionReportId != null) {
            condition = condition.and(GROUPREPORT.REGION_REPORT_ID.eq(regionReportId));
        }

        GroupreportRecord record = dsl.selectFrom(GROUPREPORT)
                .where(condition)
                .limit(1)
                .fetchAny(); // avoid TooManyRowsException

        if (record == null) {
            return null; // or throw exception if you prefer
        }

        // 🔹 Ammunition
        String ammunitionJson = String.valueOf(record.get(GROUPREPORT.AMMUNITION));

        // === Manual mapping ===
        GroupReportDto dto = new GroupReportDto();
        dto.setGroupId(record.get(GROUPREPORT.GROUP_ID));
        dto.setGroupName(record.get(GROUPREPORT.GROUP_ID).toString());
        dto.setRegionReportId(record.get(GROUPREPORT.REGION_REPORT_ID));
        dto.setFightingPlaces(Arrays.stream(record.get(GROUPREPORT.FIGHTING_PLACES)).toList()); // check if this is Array or JSONB
        dto.setRestPlaces(Arrays.stream(record.get(GROUPREPORT.REST_PLACES)).toList()); // check if this is Array or JSONB
        dto.setFightingContractors(Arrays.stream(record.get(GROUPREPORT.FIGHTING_CONTRACTORS)).toList()); // same here
        dto.setRestContractors(Arrays.stream(record.get(GROUPREPORT.REST_CONTRACTORS)).toList()); // same here
        dto.setDescription(record.get(GROUPREPORT.DESCRIPTION));
        dto.setWorked(record.get(GROUPREPORT.WORKED));
        dto.setReportDate(record.get(GROUPREPORT.REPORT_DATE));
        dto.setAmmunition(ammunitionJson);
        dto.setSuccessReport(record.get(GROUPREPORT.SUCCESS_REPORT));

        // 👇 Handle status manually if it's Boolean in DB but Enum in code
        Boolean statusValue = record.get(GROUPREPORT.STATUS);
        if (statusValue != null) {
            dto.setStatus(statusValue ? ReportStatus.ACTIVE : ReportStatus.DELETED);
        }
        return dto;
    }

    public List<ContractorWorkReportDtoRecord> getContractorWorkDataByPeriodAndReportIds(LocalDate fromDate, LocalDate toDate,
                                                                                         List<UUID> regionReportIds) {
        return dsl.selectFrom(GROUPREPORT)
                .where(GROUPREPORT.REPORT_DATE.between(fromDate, toDate))
                .and(GROUPREPORT.REGION_REPORT_ID.in(regionReportIds))
                .and(GROUPREPORT.STATUS.eq(true))
                .fetch()
                .stream()
                .flatMap(record -> {
                    UUID[] contractorIds = record.getFightingContractors();
                    LocalDate date = record.getReportDate();
                    Boolean isWorked = record.getWorked();
                    String groupName = groupService.getGroupNameById(record.getGroupId());

                    if (contractorIds == null) return Stream.empty();

                    return Arrays.stream(contractorIds)
                            .map(id -> new ContractorWorkReportDtoRecord(id, date, isWorked, groupName));
                })
                .collect(Collectors.toList());
    }

    public List<GroupReportPrefillDto> getPrefilledGroupReports(UUID regionId, LocalDate date) throws IOException {
        // Fetch all group IDs (or you can fetch only those assigned to this region)
        List<UUID> groupIds = groupService.getGroupIdsByRegionId(regionId);
        String regionNameById = regionService.getRegionNameById(regionId);
        List<OperationReportDto> reportsByDate = operativeReportService.getOperationReportsByDate(date, regionNameById);

        List<ContractorServiceStatusDto> contractorServiceStatus = status_30_100_service
                .getContractorServiceStatus(date, regionId);

        Map<UUID, ServiceStatus> uuidServiceStatusMap = mapStatusByContractor(contractorServiceStatus, date);

        Map<UUID, OperationReportDto> reportMap = reportsByDate.stream()
                .filter(report -> report.getGroupId() != null) // optional: skip null keys
                .collect(Collectors.toMap(
                        OperationReportDto::getGroupId,
                        Function.identity(),
                        (existing, replacement) -> replacement // in case of duplicate groupIds
                ));

        List<GroupReportPrefillDto> result = groupIds.stream()
                .map(groupId -> {
                    // Fetch group entity with name (assume getGroupById returns a Group object)
                    var group = groupService.getGroupById(groupId);
                    String groupNameById = groupService.getGroupNameById(groupId);
                    // Fetch reports and assignments for this group and date
                    String fightingReport = getFightingReport(reportMap.getOrDefault(groupId, null));
                    List<PlaceDto> fightingPlaces = getFightingPlaces(regionId, reportMap.getOrDefault(groupId, null));
                    List<PlaceDto> restPlaces = getRestPlaces(regionId, date);

                    List<ContractorDto> contractorsByGroup = getContractorsByGroup(groupId);

                    List<ContractorDto> fightingContractors = filterByStatus(contractorsByGroup, uuidServiceStatusMap, ServiceStatus.HUNDRED);

                    List<ContractorDto> restContractors = filterByStatus(contractorsByGroup, uuidServiceStatusMap,
                            ServiceStatus.FIRSTDAY, ServiceStatus.THIRTY);

                    boolean isFightersAvaliable = fightingContractors.size() > 0;
                    String description = getGroupDescription(reportMap.getOrDefault(groupId, null), group, isFightersAvaliable);

                    String ammunition = getAmmunition(reportMap.getOrDefault(groupId, null));
                    boolean ammoVerified = isAmmoVerified(groupId, date, reportMap.getOrDefault(groupId, null));

                    return new GroupReportPrefillDto(
                            groupId,
                            groupNameById,
                            description,
                            fightingReport,
                            fightingContractors,
                            fightingPlaces,
                            restContractors,
                            restPlaces,
                            ammunition,
                            ammoVerified,
                            group.isFighting()
                    );
                })
                .toList();

        return result;
    }

    private boolean isAmmoVerified(UUID groupId, LocalDate date, OperationReportDto reportDto) {
        if (reportDto == null) {
            return false;
        }
        return reportDto.getIsAmmoVerified();
    }

    private String getAmmunition(OperationReportDto reportDto) {
        if (reportDto == null) {
            return "";
        }
        return reportDto.getAmmo() + System.lineSeparator() + reportDto.getAssets();
    }


    private List<ContractorDto> getRestContractors(UUID groupId, LocalDate date) {
        return contractorService.getContractorsByGroupId(groupId)
                .stream()
                .toList();
    }


    private List<ContractorDto> getContractorsByGroup(UUID groupId) {
        return contractorService.getContractorsByGroupId(groupId)
                .stream()
                .toList();
    }

    private List<ContractorDto> getFightingContractors(UUID groupId, LocalDate date) {
        return contractorService.getContractorsByGroupId(groupId)
                .stream()
                .toList();
    }

    private List<PlaceDto> getRestPlaces(UUID regionId, LocalDate date) {
        return placeService.getRestPlaceByRegionId(regionId)
                .stream()
                .toList();
    }

    private List<PlaceDto> getFightingPlaces(UUID regionId, OperationReportDto reportDto) {

        if (reportDto != null) {
            return List.of(placeService.getPlaceByIds(reportDto.getPlaceId()));
        }

        return placeService.getFightingPlaceByRegionId(regionId)
                .stream()
                .toList();
    }

    private String getFightingReport(OperationReportDto reportDto) {
        if (reportDto != null) {
            return reportDto.getResult();
        }

        return "";
    }

    private String getGroupDescription(OperationReportDto reportDto, GroupDto group, boolean isFightersAvaliable) {
        if (reportDto != null) {
            return reportDto.getDescription();
        }
        if (isFightersAvaliable) {
            return descriptionTemplateService.getContentByGroupId(UUID.fromString(group.getId()));
        } else {
            return descriptionTemplateService.getContentRestByGroupId(UUID.fromString(group.getId()));
        }
    }

    public Map<UUID, ServiceStatus> mapStatusByContractor(List<ContractorServiceStatusDto> list, LocalDate targetDate) {
        // Log null IDs
        list.stream()
                .filter(dto -> dto.getId() == null)
                .forEach(dto -> System.err.println("⚠️ Null contractor ID found: " + dto));

        // Log duplicate IDs
        Set<UUID> seen = new HashSet<>();
        list.stream()
                .map(ContractorServiceStatusDto::getId)
                .filter(Objects::nonNull)
                .filter(id -> !seen.add(id))
                .forEach(duplicateId -> System.err.println("⚠️ Duplicate contractor ID: " + duplicateId));

        // Build map safely
        return list.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(
                        ContractorServiceStatusDto::getId,
                        dto -> dto.getCalendar().getOrDefault(targetDate, ServiceStatus.UNKNOWN),
                        (existing, replacement) -> existing // Keep first occurrence
                ));
    }


    public List<ContractorDto> filterByStatus(List<ContractorDto> contractorsByGroup,
                                              Map<UUID, ServiceStatus> statusMap,
                                              ServiceStatus... targetStatuses) {
        var targetSet = Set.of(targetStatuses);

        return contractorsByGroup.stream()
                .filter(c -> targetSet.contains(statusMap.getOrDefault(c.getId(), ServiceStatus.UNKNOWN)))
                .toList();
    }


}
