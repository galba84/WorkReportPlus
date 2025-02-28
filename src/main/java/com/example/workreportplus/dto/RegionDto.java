package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.IdDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegionDto extends IdDto {
    private String regionName;
}
