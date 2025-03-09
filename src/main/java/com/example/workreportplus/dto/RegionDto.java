package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.IdDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RegionDto extends IdDto {
    private String regionName;

    public RegionDto(String regionName) {
        this.regionName = regionName;
    }
}
