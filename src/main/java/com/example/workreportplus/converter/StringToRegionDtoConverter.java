package com.example.workreportplus.converter;
import org.springframework.core.convert.converter.Converter;

import com.example.workreportplus.dto.RegionDto;
import org.springframework.stereotype.Component;

@Component
public class StringToRegionDtoConverter implements Converter<String, RegionDto> {
    @Override
    public RegionDto convert(String source) {
        return new RegionDto(source); // Assuming RegionDto has a constructor that accepts a String
    }
}