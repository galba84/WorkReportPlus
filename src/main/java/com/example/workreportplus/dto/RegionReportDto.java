package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.TrackedDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegionReportDto extends TrackedDto {

    private UUID regionId;

    @NotNull
    private LocalDate reportDate;

    private String regionDescription;

    private List<UUID> groupReportIds;

    private Boolean status;

    private Map<String, String> extraData;
    private List<UUID> arrivedContractors;
    private List<UUID> departuredContractors;

}
