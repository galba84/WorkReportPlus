package com.example.workreportplus.service;


import com.example.jooq.tables.records.GroupreportRecord;
import com.example.jooq.tables.records.RegionreportRecord;
import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.Utils.JsonUtils;
import com.example.workreportplus.Utils.SecurityUtil;
import com.example.workreportplus.dto.AmmunitionDto;
import com.example.workreportplus.dto.ContractorDto;
import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.dto.RegionReportDto;
import com.example.workreportplus.exception.DatabaseAccessException;
import com.example.workreportplus.exception.ReportNotFoundException;
import com.example.workreportplus.mapper.GroupReportMapper;
import com.example.workreportplus.mapper.RegionReportMapper;
import com.example.workreportplus.request.GroupReportRequest;
import com.example.workreportplus.request.RegionReportRequest;
import com.example.workreportplus.request.searchparams.RegionReportSearchParams;
import com.example.workreportplus.request.searchparams.SearchParams;
import com.example.workreportplus.response.DailyRegionReportResponse;
import com.example.workreportplus.response.ReportResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.jooq.Tables.REGIONREPORT;
import static com.example.jooq.tables.Groupreport.GROUPREPORT;
import static com.example.workreportplus.Utils.JsonUtils.toJsonB;

@Service
public class RegionReportService implements ReportService {
    private final DSLContext dsl;
    private final RegionReportMapper regionReportMapper;
    private final RegionService regionService;  // Now properly injected
    private final GroupReportMapper groupReportMapper;
    private final GroupReportService groupReportService;
    private final ObjectMapper objectMapper;
    private final GroupService groupService;
    private final ContractorService contractorService;

    public RegionReportService(DSLContext dsl,
                               RegionReportMapper regionReportMapper,
                               RegionService regionService,
                               GroupReportMapper groupReportMapper,
                               GroupReportService groupReportService,
                               ObjectMapper objectMapper, GroupService groupService, ContractorService contractorService) {
        this.dsl = dsl;
        this.regionReportMapper = regionReportMapper;
        this.regionService = regionService;
        this.groupReportMapper = groupReportMapper;
        this.groupReportService = groupReportService;
        this.objectMapper = objectMapper;
        this.groupService = groupService;
        this.contractorService = contractorService;
    }


    @Override
    public List<ReportResponse> getReports(SearchParams sp) {
        RegionReportSearchParams searchParams = (RegionReportSearchParams) sp;

        try {
            // Ensure groupId is a valid integer

            // Ensure dates are valid
            LocalDate startDate = searchParams.getStartDate();
            LocalDate endDate = searchParams.getEndDate();

            // Validate date range
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new RuntimeException("Start date cannot be after end date");
            }
            UUID regionIdByName = regionService.getRegionIdByName(searchParams.getRegionName());
            // Fetch reports
            Condition condition = DSL.trueCondition(); // Blank condition (always true)

            // Apply filtering only if regionIdByName is not null
            if (regionIdByName != null) {
                condition = REGIONREPORT.REGION_ID.eq(regionIdByName);
            }
            if (startDate != null) {
                condition = condition.and(REGIONREPORT.REPORT_DATE.ge(LocalDate.from(startDate.atStartOfDay())));
            }

            String statusRequestParam = searchParams.getStatus();
            if ("ACTIVE".equalsIgnoreCase(statusRequestParam) || "DELETED".equalsIgnoreCase(statusRequestParam)) {
                boolean statusBool = "ACTIVE".equalsIgnoreCase(statusRequestParam);
                condition = condition.and(REGIONREPORT.STATUS.eq(statusBool));
            }


            if (endDate != null) {
                condition = condition
                        .and(REGIONREPORT.REPORT_DATE
                                .le(LocalDate.from(endDate.atStartOfDay().plusDays(1).minusSeconds(1))));
            }

            Result<RegionreportRecord> records = dsl.selectFrom(REGIONREPORT)
                    .where(condition)
                    .fetch();

            // Convert records to ReportResponse objects
            return records.stream()

                    .map(record -> {
                        Boolean statusRaw = record.get(REGIONREPORT.STATUS);
                        ReportStatus status = ReportStatus.ACTIVE;
                        if (statusRaw != null) {
                            status = ReportStatus.fromBoolean(statusRaw);
                        }
                        return DailyRegionReportResponse.builder()
                                .id(record.get(REGIONREPORT.ID))
                                .description(record.get(REGIONREPORT.REGION_DESCRIPTION))
                                .status(status)
                                .date(record.get(REGIONREPORT.REPORT_DATE))
                                .regionName(regionService.getRegionNameById(record.get(REGIONREPORT.REGION_ID)))
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            // Handle invalid group ID
            throw new DatabaseAccessException("Invalid group ID: " + searchParams.getRegionName(), e);
        } catch (DataAccessException e) {
            // Handle database access exceptions
            throw new DatabaseAccessException("Failed to fetch reports", e);
        } catch (NullPointerException e) {
            // Handle null pointer exceptions (e.g., if startDate or endDate is null)
            throw new DatabaseAccessException("Start or end date is null", e);
        }
    }


    @Override
    public ReportResponse getReportById(UUID id) {
        try {
            Condition condition = REGIONREPORT.ID.eq(id);

            Result<RegionreportRecord> records = dsl.selectFrom(REGIONREPORT)
                    .where(condition)
                    .fetch();

            // Convert records to ReportResponse objects
            if (records.isEmpty()) {
                throw new ReportNotFoundException("Report with ID " + id + " not found");
            }

            return records.get(0)

                    .map(record -> {
                                Boolean statusRaw = record.get(REGIONREPORT.STATUS);
                                ReportStatus status = ReportStatus.ACTIVE;
                                if (statusRaw != null) {
                                    status = ReportStatus.fromBoolean(statusRaw);
                                }
                                return DailyRegionReportResponse.builder()
                                        .id(record.get(REGIONREPORT.ID))
                                        .description(record.get(REGIONREPORT.REGION_DESCRIPTION))
                                        .status(status)
                                        .extraData(JsonUtils.jsonbToMap(record.get(REGIONREPORT.EXTRA_DATA)))
                                        .arrivedContractors(Arrays.asList(record.get(REGIONREPORT.ARRIVED_CONTRACTORS)))
                                        .departedContractors(populateContractorData(record.get(REGIONREPORT.DEPARTED_CONTRACTORS)))
                                        .date(record.get(REGIONREPORT.REPORT_DATE))
                                        .regionName(regionService.getRegionNameById(record.get(REGIONREPORT.REGION_ID)))
                                        .createdOn(record.get(REGIONREPORT.CREATED_ON).toLocalDate())
                                        .updatedOn(record.get(REGIONREPORT.UPDATED_ON).toLocalDate())
                                        .createdBy(record.get(REGIONREPORT.CREATED_BY))
                                        .updatedBy(record.get(REGIONREPORT.UPDATED_BY))
                                        .groupReports(groupReportService.getReportsByRegionId(record.get(REGIONREPORT.ID)))
                                        .build();
                            }
                    );

        } catch (DataAccessException e) {
            // Handle database access exceptions
            throw new DatabaseAccessException("Failed to fetch report with ID " + id, e);
        }
    }

    private Map<UUID, ContractorDto> populateContractorData(UUID[] contractorIds) {
        if (contractorIds == null || contractorIds.length == 0) return Map.of();

        List<ContractorDto> contractorList = contractorService.getContractorsByIds(List.of(contractorIds));

        return contractorList.stream()
                .collect(Collectors.toMap(ContractorDto::getId, Function.identity()));
    }



    //save report
    public DailyRegionReportResponse saveReport(RegionReportRequest regionReportRequest) {

        RegionReportDto regionReportDto = regionReportMapper.requestToDto(regionReportRequest);
        UUID regionReportId = saveRegionReport(regionReportDto);

        List<GroupReportRequest> groupReports = regionReportRequest.getGroupReports();
        if (groupReports != null) {
            groupReports.forEach(groupReport -> {
                UUID groupIdByName = groupService.getGroupIdByName(groupReport.getGroupName());
                GroupReportDto dto = groupReportMapper.requestToDto(groupReport, regionReportDto.getRegionId().toString(), regionReportDto.getReportDate(), groupIdByName);

                if (groupIdByName != null) {
                    dto.setGroupId(groupIdByName);
                }

                dto.setDescription(groupReport.getDescription());
                dto.setSuccessReport(groupReport.getSuccessReport());
                dto.setAmmunition(groupReport.getAmmunition());
                dto.setAmmoVerified(groupReport.isAmmoVerified());
                dto.setWorked(groupReport.isWorked());
                dto.setFightingContractors(groupReport.getFightingContractors());
                dto.setFightingPlaces(groupReport.getFightingPlaces());
                dto.setRestContractors(groupReport.getRestContractors());
                dto.setRestPlaces(groupReport.getRestPlaces());

                dto.setRegionReportId(regionReportId);
                dto.setReportDate(regionReportRequest.getReportDate());
                dto.setAmmunition((groupReport.getAmmunition()));
                saveGroupReport(dto);
            });
        }

        return new DailyRegionReportResponse();
    }

    private List<AmmunitionDto> parseAmmunition(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        return Arrays.stream(raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> {
                    String[] parts = line.split("\\s*:\\s*");
                    AmmunitionDto dto = new AmmunitionDto();

                    dto.setName(parts[0].trim());

                    if (parts.length >= 2) {
                        try {
                            dto.setAmount(Integer.parseInt(parts[1].trim()));
                        } catch (NumberFormatException e) {
                            dto.setAmount(0);
                        }
                    }

                    if (parts.length >= 3) {
                        dto.setUnit(parts[2].trim());
                    }

                    return dto;
                })
                .toList();
    }



    public UUID saveRegionReport(RegionReportDto regionReportDto) {
        String currentUser = SecurityUtil.getCurrentUsername(); // Fetch user from SecurityContextHolder
        LocalDate reportDate = regionReportDto.getReportDate();

        return dsl.transactionResult(conf -> {
            DSLContext tx = DSL.using(conf);

            RegionreportRecord record = tx.newRecord(REGIONREPORT);
            record.setRegionId(regionReportDto.getRegionId());
            record.setReportDate(reportDate);
            record.setRegionDescription(regionReportDto.getRegionDescription());
            record.setArrivedContractors(setArrivedContractors(regionReportDto));
            record.setDepartedContractors(setDepartedContractors(regionReportDto));
            record.setExtraData(toJsonB(regionReportDto.getExtraData()));
            record.setCreatedBy(currentUser);
            record.setUpdatedBy(currentUser);
            record.setCreatedOn(LocalDateTime.now());
            record.setUpdatedOn(LocalDateTime.now());
            record.setStatus(regionReportDto.getStatus());

            record.store(); // Persist the new report

            UUID savedId = record.getId();

            // Mark all other reports for the same date as false
            tx.update(REGIONREPORT)
                    .set(REGIONREPORT.STATUS, false)
                    .where(REGIONREPORT.REPORT_DATE.eq(reportDate))
                    .and(REGIONREPORT.REGION_ID.eq(regionReportDto.getRegionId()))
                    .and(REGIONREPORT.ID.ne(savedId))
                    .execute();

            return savedId;
        });
    }


    private static UUID[] setDepartedContractors(RegionReportDto regionReportDto) {
        if (regionReportDto.getDepartedContractors() == null) {
            return new UUID[0];
        }
        return regionReportDto.getDepartedContractors().toArray(new UUID[0]);
    }

    private static UUID[] getValue(RegionReportDto regionReportDto) {
        return getArray(regionReportDto);
    }

    private static UUID[] getArray(RegionReportDto regionReportDto) {
        return regionReportDto.getDepartedContractors().toArray(new UUID[0]);
    }

    private static UUID[] setArrivedContractors(RegionReportDto regionReportDto) {
        if (regionReportDto.getArrivedContractors() == null) {
            return new UUID[0];
        }
        return regionReportDto.getArrivedContractors().toArray(new UUID[0]);
    }


    public static Boolean fromInt(int value) {
        return value != 0; // Returns true if value is not 0, otherwise false
    }

    public void saveGroupReport(GroupReportDto groupReportDto) {
        String currentUser = SecurityUtil.getCurrentUsername();

        dsl.transaction(configuration -> {
            DSLContext tx = DSL.using(configuration);

            GroupreportRecord record = tx.newRecord(GROUPREPORT);
            record.setGroupId(groupReportDto.getGroupId());
            record.setRegionReportId(groupReportDto.getRegionReportId());
            record.setReportDate(groupReportDto.getReportDate());
            // Set status default
            Boolean statusToSet = Boolean.TRUE;
            if (groupReportDto.getStatus() != null) {
                statusToSet = fromInt(groupReportDto.getStatus().getValue());
                record.setStatus(statusToSet);
            }


            record.setDescription(groupReportDto.getDescription());
            record.setSuccessReport(groupReportDto.getSuccessReport());
            record.setFightingContractors(groupReportDto.getFightingContractors().toArray(UUID[]::new));
            record.setFightingPlaces(groupReportDto.getFightingPlaces().toArray(UUID[]::new));
            record.setRestContractors(groupReportDto.getRestContractors().toArray(UUID[]::new));
            record.setRestPlaces(groupReportDto.getRestPlaces().toArray(UUID[]::new));


            try {
                String json = objectMapper.writeValueAsString(groupReportDto.getAmmunition());
                record.setAmmunition(org.jooq.JSONB.valueOf(json));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize extraDataGroupReport", e);
            }


            // ✅ Merge extra data and place coefficients
            Map<String, String> extraData = new HashMap<>();
            if (groupReportDto.getExtraDataGroupReport() != null) {
                extraData.putAll(groupReportDto.getExtraDataGroupReport());
            }

            AtomicBoolean worked = new AtomicBoolean(false);

            record.setWorked(worked.get());

            try {
                String extraJson = objectMapper.writeValueAsString(extraData);
                record.setExtraDataGroupReport(org.jooq.JSONB.valueOf(extraJson));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize extraDataGroupReport", e);
            }

            record.setCreatedBy(currentUser);
            record.setUpdatedBy(currentUser);
            record.setCreatedOn(LocalDateTime.now());
            record.setUpdatedOn(LocalDateTime.now());

            // Save current group report
            record.store();

            UUID savedId = record.getId();
            LocalDate reportDate = groupReportDto.getReportDate();
            UUID regionReportId = groupReportDto.getRegionReportId();

            // ✅ Check parent region report status
            Boolean regionStatus = tx.select(REGIONREPORT.STATUS)
                    .from(REGIONREPORT)
                    .where(REGIONREPORT.ID.eq(regionReportId))
                    .fetchOne(REGIONREPORT.STATUS);

            // ✅ If region report is false, disable all group reports for same reportDate except the new one
            if (Boolean.FALSE.equals(regionStatus)) {
                tx.update(GROUPREPORT)
                        .set(GROUPREPORT.STATUS, false)
                        .where(GROUPREPORT.REPORT_DATE.eq(reportDate))
                        .and(GROUPREPORT.ID.ne(savedId))
                        .execute();
            }
        });
    }

//    private void setAmmunition(GroupReportDto groupReportDto, GroupreportRecord record) throws JsonProcessingException {
//        List<AmmunitionDto> ammunitionList = groupReportDto.getAmmunition();
//        if (ammunitionList == null || ammunitionList.isEmpty()) {
//            record.setAmmunition(JSONB.valueOf("[]"));
//            return;
//        }
//
//        ArrayNode jsonArray = objectMapper.createArrayNode();
//
//        for (AmmunitionDto ammo : ammunitionList) {
//            if (ammo.getName() == null || ammo.getName().trim().isEmpty()) continue;
//
//            ObjectNode entry = objectMapper.createObjectNode();
//            entry.put("name", ammo.getName().trim());
//
//            if (ammo.getAmount() != 0) {
//                entry.put("amount", ammo.getAmount());
//            }
//
//            if (ammo.getUnit() != null && !ammo.getUnit().isBlank()) {
//                entry.put("unit", ammo.getUnit().trim());
//            }
//
//            jsonArray.add(entry);
//        }
//
//        String ammoJson = objectMapper.writeValueAsString(jsonArray);
//        record.setAmmunition(JSONB.valueOf(ammoJson));
//    }


    public RegionReportDto getReportByRegionIdAndDate(UUID regionId, LocalDate reportDate) {
        Condition condition = REGIONREPORT.REGION_ID.eq(regionId);
        if (reportDate != null) {
            condition = condition.and(REGIONREPORT.REPORT_DATE.eq(reportDate));
        }
        condition = condition.and(REGIONREPORT.STATUS.eq(Boolean.TRUE));
        return dsl.selectFrom(REGIONREPORT)
                .where(condition)
                .limit(1)
                .fetchOneInto(RegionReportDto.class);
    }

    public boolean existsByRegionIdAndDate(UUID regionId, LocalDate reportDate) {
        // Базова умова: фільтруємо по regionId і статусу = true
        Condition condition = REGIONREPORT.REGION_ID.eq(regionId)
                .and(REGIONREPORT.STATUS.eq(true));

        // Додаємо перевірку дати, якщо вона задана
        if (reportDate != null) {
            condition = condition.and(REGIONREPORT.REPORT_DATE.eq(reportDate));
        }

        // Використовуємо jOOQ fetchExists для перевірки, чи є хоч один такий запис
        return dsl.fetchExists(
                DSL.selectOne()
                        .from(REGIONREPORT)
                        .where(condition)
        );
    }


    public UUID getLastReportIdByDate(LocalDate reportDate, UUID regionId) {
        return dsl.select(REGIONREPORT.ID)
                .from(REGIONREPORT)
                .where(REGIONREPORT.REPORT_DATE.eq(reportDate))
                .and(REGIONREPORT.REGION_ID.eq(regionId))
                .orderBy(REGIONREPORT.CREATED_ON.desc())
                .limit(1)
                .fetchOneInto(UUID.class);
    }

    @Override
    public List<UUID> getReportIdsByRegionAndPeriod(UUID regionId, LocalDate fromDate, LocalDate toDate) {
        Condition condition = REGIONREPORT.REGION_ID.eq(regionId);
        condition = condition.and(REGIONREPORT.STATUS.eq(true));
        if (fromDate != null) {
            condition = condition.and(REGIONREPORT.REPORT_DATE.ge(fromDate));
        }

        if (toDate != null) {
            condition = condition.and(REGIONREPORT.REPORT_DATE.le(toDate));
        }

        return dsl.select(REGIONREPORT.ID)
                .from(REGIONREPORT)
                .where(condition)
                .fetchInto(UUID.class);
    }

}
