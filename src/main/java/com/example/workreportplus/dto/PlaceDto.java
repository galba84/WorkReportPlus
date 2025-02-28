package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.AreaType;
import com.example.workreportplus.dto.templates.IdDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlaceDto extends IdDto {
    String name;
    AreaType areaType;
    String county;
    String district;
    String region;
}
