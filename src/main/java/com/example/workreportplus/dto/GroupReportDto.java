package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.dto.templates.TrackedDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupReportDto extends TrackedDto {

    private UUID groupId;

    private String groupName;

    private String regionName;

    private UUID regionReportId;

    private List<UUID> fightingContractors;
    private List<UUID> restContractors;

    private List<UUID> fightingPlaces;
    private List<UUID> restPlaces;

    private String description;
    private String successReport;

    private String ammunition;
    private boolean ammoVerified;

    private boolean worked;

    private Map<String,String> extraDataGroupReport;

    private ReportStatus status;

    private LocalDate reportDate;

    private Map<String, String> extraData;

}
