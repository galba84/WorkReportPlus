package com.example.workreportplus.mapper;


import com.example.workreportplus.dto.RegionReportDto;
import com.example.workreportplus.request.RegionReportRequest;
import com.example.workreportplus.service.RegionService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class RegionReportMapper {

    @Autowired
    protected RegionService regionService;  // Now properly injected

    public abstract RegionReportDto requestToDto(RegionReportRequest request);

    @AfterMapping
    protected void setGroupId(@MappingTarget RegionReportDto dto, RegionReportRequest request) {

        dto.setRegionId(regionService.getRegionIdByName(request.getRegionName()));
        if (request.getGroupReports() == null) {
            dto.setGroupReportIds(List.of());
        }
    }
}
