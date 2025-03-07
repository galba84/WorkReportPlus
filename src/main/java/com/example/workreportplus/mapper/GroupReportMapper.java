package com.example.workreportplus.mapper;

import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.request.GroupReportRequest;
import com.example.workreportplus.service.GroupService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class GroupReportMapper {

    @Autowired
    protected GroupService groupService;  // Now properly injected

    public abstract GroupReportDto requestToDto(GroupReportRequest request);

    @AfterMapping
    protected void setGroupId(@MappingTarget GroupReportDto dto, GroupReportRequest request) {
        UUID groupId = groupService.getGroupIdByName(request.getGroupName());
        if (groupId == null) {
            return;
        }
        dto.setGroupId(groupId);
    }
}
