package com.example.workreportplus.dto;

import com.example.workreportplus.dto.templates.IdDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupDto extends IdDto {

    private String name;

    private UUID regionId;

    List<ContractorDto> contractors;
}
