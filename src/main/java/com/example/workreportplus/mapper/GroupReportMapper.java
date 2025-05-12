package com.example.workreportplus.mapper;

import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.request.GroupReportRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class GroupReportMapper {

    public abstract GroupReportDto requestToDto(GroupReportRequest request, @Context String regionId, @Context LocalDate reportDate, @Context UUID groupId);

    @AfterMapping
    protected void setGroupIdAndConvertLists(@MappingTarget GroupReportDto dto,
                                             GroupReportRequest request,
                                             @Context String regionId,
                                             @Context LocalDate reportDate,
                                             @Context UUID groupId) {
        if (groupId != null) {
            dto.setGroupId(groupId);
        }
        dto.setPlacesWithCoeficcient(dto.getPlacesWithCoeficcient());
        dto.setPlaceIds((request.getPlaceCoefficients()
                .keySet().stream()
                .filter(Objects::nonNull)
                .toList()));
        dto.setContractorsIds(safeStringListToUuidList(request.getContractorPlaceMap().keySet().stream().toList()));
        dto.setContractorToPlacesMap(convertToUUIDMap(request.getContractorPlaceMap()));
        dto.setPlacesWithCoeficcient(request.getPlaceCoefficients());
        dto.setRegionReportId(UUID.fromString(regionId));
        dto.setReportDate(reportDate);
    }

    public static List<UUID> safeStringListToUuidList(List<String> input) {
        if (input == null) return List.of();
        return input.stream()
                .filter(s -> s != null && !s.isBlank() && !"null".equalsIgnoreCase(s))
                .map(String::trim)
                .map(UUID::fromString)
                .toList();
    }

    public static Map<UUID, List<UUID>> convertToUUIDMap(Map<String, List<String>> input) {
        if (input == null) return Collections.emptyMap();

        return input.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey()),
                        entry -> entry.getValue().stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toList())
                ));
    }
}
