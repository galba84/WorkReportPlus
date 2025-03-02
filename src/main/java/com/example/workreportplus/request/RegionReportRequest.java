package com.example.workreportplus.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class RegionReportRequest {

    @NotNull
    private String reportDate;

    @NotBlank
    private String region;

    private String regionDescription;

    private List<GroupReportRequest> groupReports;

    private Map<String, String> extraData;
    private String status;

}
