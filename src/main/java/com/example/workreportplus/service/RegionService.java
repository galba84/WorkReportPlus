package com.example.workreportplus.service;

import com.example.workreportplus.dto.RegionDto;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.jooq.Tables.REGION;
import static com.example.workreportplus.service.GoogleSheetsService.DOVIDNYK_TABLE_ID;

@Service
public class RegionService {

    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public RegionService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public UUID getRegionIdByName(String name) {
        return dsl.select(REGION.ID)
                .from(REGION)
                .where(REGION.REGION_NAME.eq(name)
                        .and(REGION.STATUS.isTrue()))
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
                .where(REGION.STATUS.isTrue())
                .fetchInto(String.class);
    }

    public List<RegionDto> getRegions() {
        return dsl.selectFrom(REGION)
                .where(REGION.STATUS.isTrue())
                .fetchInto(RegionDto.class);
    }

    public List<RegionDto> getAllRegions() {
        return dsl.selectFrom(REGION)
                .fetchInto(RegionDto.class);
    }

    public String getRegionNameById(UUID id) {
        return dsl.select(REGION.REGION_NAME)
                .from(REGION)
                .where(REGION.ID.eq(id))
                .fetchOne(REGION.REGION_NAME);
    }

    public void updateRegionsFromTable() throws IOException {
        String range = "RegionList!A2:B"; // id, name

        List<List<Object>> rows = googleSheetsService.readSheet(DOVIDNYK_TABLE_ID, range);

        if (rows.isEmpty()) {
            System.out.println("No data found in RegionList sheet");
            return;
        }

        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            // Step 1: Set all existing region statuses to false
            ctx.update(REGION)
                    .set(REGION.STATUS, false)
                    .execute();

            // Step 2: Upsert all rows from sheet
            List<Query> upserts = rows.stream()
                    .filter(row -> row.size() >= 2)
                    .map(row -> {
                        UUID regionId = !row.get(0).toString().isEmpty()
                                ? UUID.fromString(row.get(0).toString())
                                : UUID.randomUUID();

                        String regionName = row.get(1).toString().trim();

                        return (Query) ctx.insertInto(REGION)
                                .set(REGION.ID, regionId)
                                .set(REGION.REGION_NAME, regionName)
                                .set(REGION.STATUS, true)
                                .onConflict(REGION.ID)
                                .doUpdate()
                                .set(REGION.REGION_NAME, regionName)
                                .set(REGION.STATUS, true);
                    })
                    .toList();

            ctx.batch(upserts).execute();
        });

        System.out.println("✅ Regions updated successfully from sheet.");
    }


    public Optional<RegionDto> getByName(String regionName) {
        return dsl.selectFrom(REGION)
                .where(REGION.REGION_NAME.eq(regionName))
                .fetchOptionalInto(RegionDto.class);
    }

    public boolean regionExistsById(UUID regionUUID) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(REGION)
                        .where(REGION.ID.eq(regionUUID))
        );
    }

    public void createRegion(RegionDto regionDto) {
        dsl.insertInto(REGION)
                .set(REGION.ID, regionDto.getId() != null ? UUID.fromString(regionDto.getId()) : UUID.randomUUID())
                .set(REGION.REGION_NAME, regionDto.getRegionName())
                .set(REGION.STATUS, true)
                .execute();
    }

    public void updateRegion(UUID id, RegionDto regionDto) {
        dsl.update(REGION)
                .set(REGION.REGION_NAME, regionDto.getRegionName())
                .set(REGION.STATUS, regionDto.getStatus())
                .where(REGION.ID.eq(id))
                .execute();
    }

    public void softDeleteRegion(UUID id) {
        dsl.update(REGION)
                .set(REGION.STATUS, false)
                .where(REGION.ID.eq(id))
                .execute();
    }

    public Optional<RegionDto> getRegionById(UUID id) {
        return dsl.selectFrom(REGION)
                .where(REGION.ID.eq(id).and(REGION.STATUS.isTrue()))
                .fetchOptionalInto(RegionDto.class);
    }

    public Map<UUID, String> getAllRegionIdNameMap() {
        return dsl.select(REGION.ID, REGION.REGION_NAME)
                .from(REGION)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record1 -> record1.get(REGION.ID),
                        record1 -> record1.get(REGION.REGION_NAME)
                ));
    }

}
