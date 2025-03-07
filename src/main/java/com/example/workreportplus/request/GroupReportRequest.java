package com.example.workreportplus.request;

import com.example.workreportplus.ENUM.PersonnelLossesType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GroupReportRequest implements ReportRequest {
    @NotBlank
    private String groupName;

    @NotBlank
    private String regionName;


    @NotBlank
    private List<String> placeIds;

    @NotBlank
    List<String> contractorsIds;

    @NotBlank
    private String description;

    @NotBlank
    private Map<String, PersonnelLossesType> contractorLoosesIdTypeMap;

    private boolean worked;

    private Map<String,String> extraDataGroupReport;
}
