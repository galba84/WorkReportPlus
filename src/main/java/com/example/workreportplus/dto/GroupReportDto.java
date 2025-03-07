package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.PersonnelLossesType;
import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.dto.templates.TrackedDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupReportDto extends TrackedDto {

    private UUID groupId;

    private String groupName;

    private String regionName;

    private UUID regionReportId;

    private List<UUID> placeIds;

    private List<UUID> contractorsIds;

    private String description;

    private Map<String, PersonnelLossesType> contractorLoosesIdTypeMap;

    private boolean worked;

    private Map<String,String> extraDataGroupReport;

    private ReportStatus status;
}
