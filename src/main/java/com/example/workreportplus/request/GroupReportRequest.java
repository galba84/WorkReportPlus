package com.example.workreportplus.request;

import com.example.workreportplus.ENUM.PersonnelLossesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GroupReportRequest implements ReportRequest {

    @NotBlank
    private String groupName;

    @NotEmpty(message = "At least one place must be selected with a coefficient")
    private Map<UUID, String> placeCoefficients; // key = placeId, value = coefficient

    @NotEmpty(message = "At least one contractor must be assigned")
    private Map<String, List<String>> contractorPlaceMap; // key = contractorId, value = list of placeIds

    @NotBlank
    private String description;

    private String ammunition; // ✅ Also correct


    private Map<String, PersonnelLossesType> contractorLoosesIdTypeMap;

    private boolean worked;

    private Map<String, String> extraDataGroupReport;
}
