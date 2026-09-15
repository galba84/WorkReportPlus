package com.example.workreportplus.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RegionReportRequest implements ReportRequest {

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // Ensures correct JSON serialization
    private LocalDate reportDate;

    @NotBlank
    private String regionName;

    private String regionDescription;

    @Valid
    @NotNull
    private List<@Valid GroupReportRequest> groupReports;


    private Map<String, String> extraData;
    private String status;
    private List<UUID> arrivedContractors;
    private List<UUID> departedContractors;

}
