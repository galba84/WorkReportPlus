package com.example.workreportplus.response;

import com.example.workreportplus.ENUM.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class DailyGroupReportResponse implements ReportResponse {

    private UUID id;

    private UUID regionReportId;

    private String regionName;

    private LocalDate date;

    private String createdBy;
    private String updatedBy;
    private LocalDate createdOn;
    private LocalDate updatedOn;

    private List<WorkingAreaResponse> workingAreas;

    private List<ContractorResponse> contractors;

    private Map<String, String> contractorLooses;

    private String description;

    private boolean worked;

    private ReportStatus status;

    private Map<String, String> extraData;

    private String groupReportId;

}