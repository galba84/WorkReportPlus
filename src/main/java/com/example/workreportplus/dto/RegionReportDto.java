package com.example.workreportplus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RegionReportDto {

    @NotNull
    private String reportDate;

    @NotBlank
    private String region;

    private String regionDescription;

    private List<GroupReportDto> groupReports;

    // Getters and Setters
    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionDescription() {
        return regionDescription;
    }

    public void setRegionDescription(String regionDescription) {
        this.regionDescription = regionDescription;
    }

    public List<GroupReportDto> getGroupReports() {
        return groupReports;
    }

    public void setGroupReports(List<GroupReportDto> groupReports) {
        this.groupReports = groupReports;
    }
}
