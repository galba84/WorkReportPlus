package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.PersonnelLossesType;
import com.example.workreportplus.dto.templates.TrackedDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupReportDto extends TrackedDto {
    @NotBlank
    private GroupDto group;

    @NotBlank
    private List<PlaceDto> places;

    @NotBlank
    private String description;

    @NotBlank
    private Map<String, PersonnelLossesType> contractorLoosesIdTypeMap;

    private boolean isWorked;

    private Map<String,String> extraDataGroupReport;
}
