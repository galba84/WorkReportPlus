package com.example.workreportplus.dto;

/**
 * @author Alex Sereda
 * @date 10.06.2025 17:47
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionReportTemplateDto {
    private UUID id;
    private UUID regionId;
    private String preamble;
    private String signature;
    private String createdBy;
    private String updatedBy;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;

    private String fileFormat;
    private String content;
    private List<String> variables;
}

