package com.example.workreportplus.mapper;

import com.example.workreportplus.dto.GroupReportDto;
import com.example.workreportplus.request.GroupReportRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RegionReportMapper {
    RegionReportMapper INSTANCE = Mappers.getMapper(RegionReportMapper.class);

    // Correct the mapping to directly set `regionName`
    GroupReportDto requestToDto(GroupReportRequest request);

}
