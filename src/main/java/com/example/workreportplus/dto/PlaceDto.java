package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.IdDto;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDto extends IdDto {
    String name;
    String areaType;
    String county;
    String district;
    String region;
    String regionId;
    String coeficient;
}
