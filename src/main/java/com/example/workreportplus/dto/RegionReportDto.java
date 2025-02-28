package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.TrackedDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegionReportDto extends TrackedDto {

    @NotNull
    private String reportDate;

    @NotBlank
    private RegionDto region;

    private String regionDescription;

    private List<GroupReportDto> groupReports;

    private Map<String, String> extraData;

}
