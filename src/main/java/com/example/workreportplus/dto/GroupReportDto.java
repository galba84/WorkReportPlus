package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.PersonnelLossesType;
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

    private List<UUID> placeIds;
    private Map<UUID, String> placesWithCoeficcient;
    private Map<UUID, List<UUID>> contractorToPlacesMap;

    private List<UUID> contractorsIds;
    private List<ContractorDto> contractors;

    private String description;
    private String details;

    private Map<String, PersonnelLossesType> contractorLoosesIdTypeMap;

    private boolean worked;

    private Map<String,String> extraDataGroupReport;

    private ReportStatus status;

    private LocalDate reportDate;

    private Map<String, String> extraData;

}
