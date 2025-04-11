package com.example.workreportplus.mapper;

import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.request.GroupReportRequest;
import com.example.workreportplus.service.GroupService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class GroupReportMapper {

    @Autowired
    protected GroupService groupService;

    public abstract GroupReportDto requestToDto(GroupReportRequest request);

    @AfterMapping
    protected void setGroupIdAndConvertLists(@MappingTarget GroupReportDto dto, GroupReportRequest request) {
        UUID groupId = groupService.getGroupIdByName(request.getGroupName());
        if (groupId != null) {
            dto.setGroupId(groupId);
        }
        dto.setPlacesWithCoeficcient(dto.getPlacesWithCoeficcient());
        dto.setPlaceIds(safeStringListToUuidList(request.getPlaceCoefficients()
                .keySet().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank() && !"null".equalsIgnoreCase(s)).toList()));
        dto.setContractorsIds(safeStringListToUuidList(request.getContractorsIds()));
    }

    protected List<UUID> safeStringListToUuidList(List<String> input) {
        if (input == null) return List.of();
        return input.stream()
                .filter(s -> s != null && !s.isBlank() && !"null".equalsIgnoreCase(s))
                .map(String::trim)
                .map(UUID::fromString)
                .toList();
    }
}
