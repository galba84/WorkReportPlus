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
        List<UUID> fightingContractors,
        List<UUID> fightingPlaces,
        List<UUID> restContractors,
        List<UUID> restPlaces,
        String ammunition,
        boolean ammoVerified,
        boolean fightingGroup
) {

}

