package com.example.workreportplus.dto;

import java.util.List;
import java.util.UUID;

/**
 * @author Alex Sereda
 * @date 24.05.2025 19:18
 */
public record GroupReportPrefillDto(
        UUID groupId,
        String groupName,
        String description,
        String fightingReport,
        List<ContractorDto> fightingContractors,
        List<PlaceDto> fightingPlaces,
        List<ContractorDto> restContractors,
        List<PlaceDto> restPlaces,
        String ammunition,
        boolean ammoVerified,
        boolean isFighting
) {

}

