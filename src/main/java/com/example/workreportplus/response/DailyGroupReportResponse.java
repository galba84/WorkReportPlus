package com.example.workreportplus.response;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@Data
public class DailyGroupReportResponse {

    private String id;

    private String regionReportId;

    @NotBlank
    private String region;

    private LocalDate date;

    @NotBlank
    private String createdBy;

    private String status;
    @NotBlank
    private List<WorkingAreaResponse> workingAreas;

    private List<ContractorResponse> contractors;

    private List<ContractorLoosesResponse> contractorLooses;

    private String description;

}