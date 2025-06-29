package com.example.workreportplus.dto;

/**
 * @author Alex Sereda
 * @date 27.06.2025 21:20
 */

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSettingDto {
    private String settingKey;
    private String settingValue;
    private JsonNode settingData;
    private String format;
    private String description;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
}