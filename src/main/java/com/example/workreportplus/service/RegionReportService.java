package com.example.workreportplus.service;


import com.example.jooq.tables.records.GroupreportRecord;
import com.example.jooq.tables.records.RegionreportRecord;
import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.Utils.JsonUtils;
import com.example.workreportplus.Utils.SecurityUtil;
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
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    public RegionReportService(DSLContext dsl, RegionReportMapper regionReportMapper, RegionService regionService, GroupReportMapper groupReportMapper, GroupReportService groupReportService) {
        this.dsl = dsl;
        this.regionReportMapper = regionReportMapper;
        this.regionService = regionService;
        this.groupReportMapper = groupReportMapper;
        this.groupReportService = groupReportService;
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
                condition = condition.and(REGIONREPORT.CREATED_ON.ge(startDate.atStartOfDay()));
            }
            if (endDate != null) {
                condition = condition.and(REGIONREPORT.CREATED_ON.le(endDate.atStartOfDay().plusDays(1).minusSeconds(1)));
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


    //save report
    public DailyRegionReportResponse saveReport(RegionReportRequest report) {

        RegionReportDto regionReportDto = regionReportMapper.requestToDto(report);
        UUID regionReportId = saveRegionReport(regionReportDto);

        List<GroupReportRequest> groupReports = report.getGroupReports();
        if (groupReports != null) {
            groupReports.forEach(groupReport -> {
                GroupReportDto groupReportDto = groupReportMapper.requestToDto(groupReport);
                groupReportDto.setRegionReportId(regionReportId);
                groupReportDto.setReportDate(report.getReportDate());
                saveGroupReport(groupReportDto);
            });
        }

        return new DailyRegionReportResponse();
    }

    public UUID saveRegionReport(RegionReportDto regionReportDto) {
        String currentUser = SecurityUtil.getCurrentUsername(); // Fetch user from SecurityContextHolder
        // Date formatter (adjust format based on your input format)


        // Convert Strings to LocalDate safely
        LocalDate reportDate = regionReportDto.getReportDate();

        RegionreportRecord record = dsl.newRecord(REGIONREPORT);
        record.setRegionId(regionReportDto.getRegionId()); // Set the group ID
        record.setReportDate(reportDate);
        record.setRegionDescription(regionReportDto.getRegionDescription());
        record.setArrivedContractors(setArrivedContractors(regionReportDto));
        record.setDepartedContractors(setDepartedContractors(regionReportDto));
        record.setExtraData(toJsonB(regionReportDto.getExtraData()));
        record.setCreatedBy(currentUser); // Store the logged-in user
        record.setUpdatedBy(currentUser);
        record.setCreatedOn(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());
        record.setStatus(regionReportDto.getStatus());
        record.store(); // Saves the record

        // Assuming RegionreportRecord has a method to get its ID
        // If the ID is stored as an Integer in the database, this should work fine
        return record.getId(); // Return the ID as an Integer
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
        String currentUser = SecurityUtil.getCurrentUsername(); // ✅ Fetch user from SecurityContextHolder

        GroupreportRecord record = dsl.newRecord(GROUPREPORT);
        record.setGroupId(UUID.fromString(groupReportDto.getGroupName())); // ✅ Set the group ID
        record.setRegionReportId(groupReportDto.getRegionReportId());
        record.setReportDate(groupReportDto.getReportDate());
        if (groupReportDto.getStatus() != null) {
            record.setStatus(fromInt(groupReportDto.getStatus().getValue()));
        } else {
            record.setStatus(Boolean.TRUE);
        }
        record.setIsWorked(groupReportDto.isWorked());
        record.setDescription(groupReportDto.getDescription());
        if (groupReportDto.getContractorsIds() != null) {
            record.setContractorsIds(groupReportDto.getContractorsIds().toArray(new UUID[0])); // ✅ Convert List<UUID> to UUID[]
        }
        if (groupReportDto.getPlaceIds() != null) {
            record.setPlaceIds(groupReportDto.getPlaceIds().toArray(new UUID[0])); // ✅ Convert List<UUID> to UUID[]
        }

        record.setCreatedBy(currentUser); // ✅ Store the logged-in user
        record.setUpdatedBy(currentUser);
        record.setCreatedOn(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());
        record.store(); // ✅ Saves the record
    }

    public RegionReportDto getReportByRegionIdAndDate(UUID regionId, LocalDate reportDate) {
        Condition condition = REGIONREPORT.REGION_ID.eq(regionId);
        if (reportDate != null) {
            condition = condition.and(REGIONREPORT.REPORT_DATE.eq(reportDate));
        }

        return dsl.selectFrom(REGIONREPORT)
                .where(condition)
                .limit(1)
                .fetchOneInto(RegionReportDto.class);
    }
}
