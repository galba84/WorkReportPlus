package com.example.workreportplus.response;

import com.example.workreportplus.ENUM.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DailyGroupReportResponse implements ReportResponse {

    private UUID id;

    private UUID regionReportId;

    private String regionName;

    private LocalDate date;

    private String createdBy;

    private List<WorkingAreaResponse> workingAreas;

    private List<ContractorResponse> contractors;

    private List<ContractorLoosesResponse> contractorLooses;

    private String description;

    private boolean worked;

    private ReportStatus status;

}