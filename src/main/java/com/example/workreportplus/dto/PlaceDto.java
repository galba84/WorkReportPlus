package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.IdDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlaceDto extends IdDto {
    String name;
    String areaType;
    String county;
    String district;
    String region;
    String regionId;
    String coeficient;
}
