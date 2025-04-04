package com.example.workreportplus.service;

import com.example.workreportplus.dto.RegionDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.example.jooq.Tables.REGION;

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

    public void updateRegionsFromTable() throws IOException {
        String sheetId = "1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss"; // or inject as a property
        String range = "RegionList!A2:B"; // id, name

        List<List<Object>> rows = googleSheetsService.readSheet(sheetId, range);

        if (rows.isEmpty()) {
            System.out.println("No data found in RegionList sheet");
            return;
        }

        dsl.batch(
                rows.stream()
                        .filter(row -> row.size() >= 2) // minimal required columns
                        .map(row -> {
                            UUID regionId = !row.get(0).toString().isEmpty()
                                    ? UUID.fromString(row.get(0).toString())
                                    : UUID.randomUUID();

                            String regionName = row.get(1).toString().trim();

                            return dsl.insertInto(REGION)
                                    .set(REGION.ID, regionId)
                                    .set(REGION.REGION_NAME, regionName)
                                    .onConflict(REGION.ID)
                                    .doUpdate()
                                    .set(REGION.REGION_NAME, regionName);
                        })
                        .toList()
        ).execute();

        System.out.println("Regions updated successfully from sheet.");
    }

}
