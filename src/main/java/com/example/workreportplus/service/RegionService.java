package com.example.workreportplus.service;

import com.example.workreportplus.dto.RegionDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.jooq.Tables.REGION;

@Service
public class RegionService {

    private final DSLContext dsl;

    public RegionService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public UUID getRegionIdByName(String name) {
        return dsl.select(REGION.ID)
                .from(REGION)
                .where(REGION.REGION_NAME.eq(name))
                .fetchOne(REGION.ID);
    }

    public boolean regionExistsByName(String name) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(REGION)
                        .where(REGION.REGION_NAME.eq(name))
        );
    }


    public List<String> getRegionNames() {
        return dsl.select(REGION.REGION_NAME)
                .from(REGION)
                .fetchInto(String.class);
    }

    public List<RegionDto> getRegions() {
        return dsl.selectFrom(REGION)
                .fetchInto(RegionDto.class);
    }

    public String getRegionNameById(UUID id) {
        return dsl.select(REGION.REGION_NAME)
                .from(REGION)
                .where(REGION.ID.eq(id))
                .fetchOne(REGION.REGION_NAME);
    }
}
