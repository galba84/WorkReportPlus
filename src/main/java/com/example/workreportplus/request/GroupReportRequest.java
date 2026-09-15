package com.example.workreportplus.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GroupReportRequest implements ReportRequest {

    @NotBlank
    private String groupName;

    @NotBlank
    private String description;
    private String successReport;

    private String ammunition; // ✅ Also correct
    private boolean ammoVerified;


    private boolean worked;

    private Map<String, String> extraDataGroupReport;

    private List<UUID> fightingContractors;
    private List<UUID> restContractors;

    private List<UUID> fightingPlaces;
    private List<UUID> restPlaces;

}
