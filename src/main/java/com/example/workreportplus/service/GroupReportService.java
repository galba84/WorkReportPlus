package com.example.workreportplus.service;


import com.example.jooq.tables.records.GroupreportRecord;
import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.Utils.SecurityUtil;
import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.exception.DatabaseAccessException;
import com.example.workreportplus.exception.ReportNotFoundException;
import com.example.workreportplus.mapper.GroupReportMapper;
import com.example.workreportplus.mapper.RegionReportMapper;
import com.example.workreportplus.request.RegionReportRequest;
import com.example.workreportplus.request.searchparams.GroupReportSearchParams;
import com.example.workreportplus.request.searchparams.SearchParams;
import com.example.workreportplus.response.ContractorResponse;
import com.example.workreportplus.response.DailyGroupReportResponse;
import com.example.workreportplus.response.DailyRegionReportResponse;
import com.example.workreportplus.response.ReportResponse;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.jooq.tables.Groupreport.GROUPREPORT;

@Service
public class GroupReportService implements ReportService {
    private final DSLContext dsl;
    private final RegionReportMapper regionReportMapper;
    private final GroupReportMapper groupReportMapper;

    public GroupReportService(DSLContext dsl, RegionReportMapper regionReportMapper,
                              GroupReportMapper groupReportMapper) {
        this.dsl = dsl;
        this.regionReportMapper = regionReportMapper;
        this.groupReportMapper = groupReportMapper;
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
        UUID[] contractorIds = record.get(GROUPREPORT.CONTRACTORS_IDS);

        return (contractorIds == null || contractorIds.length == 0)
                ? List.of()
                : Arrays.stream(contractorIds)
                .map(UUID::toString) // ✅ Convert UUID to String
                .map(ContractorResponse::new) // ✅ Pass as String to ContractorResponse constructor
                .toList();
    }





    @Override
    public ReportResponse getReportById(UUID id) {
        try {
            // Fetch a single record into DailyGroupReportResponse
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


    //save report
    public DailyRegionReportResponse saveReport(RegionReportRequest report) {
        report.getGroupReports().forEach(groupReport -> {
            groupReport.getGroupName();
            GroupReportDto groupReportDto = groupReportMapper.requestToDto(groupReport);
            saveGroupReport(groupReportDto);
        });
        return new DailyRegionReportResponse();
    }

    public void saveGroupReport(GroupReportDto groupReportDto) {
        String currentUser = SecurityUtil.getCurrentUsername(); // ✅ Fetch user from SecurityContextHolder

        GroupreportRecord groupReportRecord = dsl.newRecord(GROUPREPORT);
        groupReportRecord.setGroupId(groupReportDto.getGroupId()); // ✅ Set the group ID
        groupReportRecord.setDescription(groupReportDto.getDescription());
        groupReportRecord.setCreatedBy(currentUser); // ✅ Store the logged-in user
        groupReportRecord.setUpdatedBy(currentUser);
        groupReportRecord.setCreatedOn(LocalDateTime.now());
        groupReportRecord.setUpdatedOn(LocalDateTime.now());
        groupReportRecord.store(); // ✅ Saves the record
    }

    private DailyGroupReportResponse mapRecordToResponse(GroupreportRecord record) {

        Boolean b = record.get(GROUPREPORT.STATUS);
        ReportStatus status = ReportStatus.ACTIVE;
        if (b != null) {
            status = ReportStatus.fromBoolean(b);
        }


        return DailyGroupReportResponse.builder()
                .regionReportId(record.get(GROUPREPORT.ID))
                .date(record.get(GROUPREPORT.REPORT_DATE))
                .description(record.get(GROUPREPORT.DESCRIPTION))
                .status(status)
                .contractors(getContractorResponses(record))
                .createdBy(record.get(GROUPREPORT.CREATED_BY))
                .build();
    }
}
