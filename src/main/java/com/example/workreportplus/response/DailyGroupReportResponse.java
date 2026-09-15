package com.example.workreportplus.response;

import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.dto.AmmunitionDto;
import com.example.workreportplus.dto.PlaceDto;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String groupName;

    private LocalDate date;

    private String createdBy;
    private String updatedBy;
    private LocalDate createdOn;
    private LocalDate updatedOn;

    private List<PlaceDto> workingAreas;

    private List<ContractorResponse> contractors;

    private List<UUID> contractorLooses;

    private String description;
    @JsonProperty("successReport")
    private String successReport;

    private boolean worked;

    private ReportStatus status;

    private Map<String, String> extraData;

    private String groupReportId;

    private List<AmmunitionDto> ammunition;

}