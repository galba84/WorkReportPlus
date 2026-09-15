package com.example.workreportplus.response;

import com.example.workreportplus.ENUM.ReportStatus;
import com.example.workreportplus.dto.ContractorDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyRegionReportResponse implements ReportResponse {

    private UUID id;
    private LocalDate date;
    private String regionName;
    private String description;
    @NotBlank
    private String createdBy;
    private LocalDate createdOn;
    @NotBlank
    private String updatedBy;
    private LocalDate updatedOn;
    private ReportStatus status;
    private Map<String, String> extraData;
    private Map<UUID, ContractorDto> arrivedContractors;
    private Map<UUID, ContractorDto> departedContractors;

    List<DailyGroupReportResponse> groupReports;
}
