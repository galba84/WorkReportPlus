package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.IdDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupDto extends IdDto {

    private String name;

    private RegionDto region;

    List<ContractorDto> contractors;
}
