package com.example.workreportplus.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyRegionReportResponse {

    private String id;
    private LocalDate date;
    private String region;
    private String description;
    @NotBlank
    private String createdBy;
    private LocalDate createdOn;
    @NotBlank
    private String updatedBy;
    private LocalDate updatedOn;
    private String status;

    List<DailyGroupReportResponse> groupReports;
}
