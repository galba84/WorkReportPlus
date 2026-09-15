package com.example.workreportplus.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Alex Sereda
 * @date 25.05.2025 17:06
 */
@Data
public class OperationReportDto {
    LocalDate date;
    UUID groupId;
    String groupName;
    UUID placeId;
    String placeName;
    String regionName;
    String description;
    String result;
    String ammo;
    String assets;
    Boolean isAmmoVerified;
}
