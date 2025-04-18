package com.example.workreportplus.request;

import com.example.workreportplus.ENUM.PersonnelLossesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GroupReportRequest implements ReportRequest {

    @NotBlank
    private String groupName;

    @NotEmpty
    private Map<String, String> placeCoefficients;

    @NotEmpty
    private List<@NotBlank String> contractorsIds;

    @NotBlank
    private String description;

    private Map<String, PersonnelLossesType> contractorLoosesIdTypeMap;

    private boolean worked;

    private Map<String, String> extraDataGroupReport;
}

