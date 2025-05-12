package com.example.workreportplus.service;

import com.example.workreportplus.dto.PlaceDto;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.example.jooq.Tables.PLACE;
import static com.example.workreportplus.service.GoogleSheetsService.DOVIDNYK_TABLE_ID;

@Service
public class PlacesService {
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public PlacesService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public UUID getPlaceIdByName(String groupName) {
        return dsl.select(PLACE.ID)
                .from(PLACE)
                .where(PLACE.NAME.eq(groupName))
                .fetchOne(PLACE.ID);
    }

    public List<PlaceDto> getAllPlaces() {
        return dsl.selectFrom(PLACE)
                .fetchInto(PlaceDto.class);
    }


    public void updatePlacesFromTable() throws IOException {
        String range = "Places!A2:I";

        List<List<Object>> rows = googleSheetsService.readSheet(DOVIDNYK_TABLE_ID, range);

        if (rows.isEmpty()) {
            System.out.println("No data found in Places sheet");
            return;
        }
        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            ctx.deleteFrom(PLACE).execute();

            ctx.batch(
                    rows.stream()
                            .filter(row -> row.size() >= 8)
                            .filter(row -> !row.get(1).toString().isBlank()) // name must not be blank
                            .map(row -> {
                                UUID id = safeParseUuid(row.get(0));
                                if (id == null) id = UUID.randomUUID();

                                String name = row.get(1).toString().trim();
                                String areaType = row.get(2).toString().trim();
                                String county = row.get(3).toString().trim(); // ✅ fix
                                String district = row.get(4).toString().trim();
                                String region = row.get(5).toString().trim();
                                String coefficient = row.get(6).toString().trim();
                                UUID regionId = safeParseUuid(row.get(7)); // can be null

                                return ctx.insertInto(PLACE)
                                        .set(PLACE.ID, id)
                                        .set(PLACE.NAME, name)
                                        .set(PLACE.AREA_TYPE, areaType)
                                        .set(PLACE.COUNTY, county) // ✅ now included
                                        .set(PLACE.DISTRICT, district)
                                        .set(PLACE.REGION, region)
                                        .set(PLACE.COEFICIENT, coefficient)
                                        .set(PLACE.REGION_ID, regionId)
                                        .onConflict(PLACE.ID)
                                        .doUpdate()
                                        .set(PLACE.NAME, name)
                                        .set(PLACE.AREA_TYPE, areaType)
                                        .set(PLACE.COUNTY, county) // ✅ now included
                                        .set(PLACE.DISTRICT, district)
                                        .set(PLACE.REGION, region)
                                        .set(PLACE.COEFICIENT, coefficient)
                                        .set(PLACE.REGION_ID, regionId);
                            })
                            .toList()
            ).execute();
        });
        System.out.println("Places updated successfully from sheet.");
    }

    private UUID safeParseUuid(Object value) {
        try {
            String str = value.toString().trim();
            if (str.isEmpty() || str.equals("-")) return null;
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID skipped: " + value);
            return null;
        }
    }


    public List<PlaceDto> getPlaceByRegionId(UUID regionId) {
        return dsl.selectFrom(PLACE)
                .where(PLACE.REGION_ID.eq(regionId))
                .fetch()
                .map(record -> {
                    PlaceDto dto = new PlaceDto();
                    dto.setId(record.getId() != null ? record.getId().toString() : null);
                    dto.setName(record.getName());
                    dto.setAreaType(record.getAreaType());
                    dto.setCounty(record.getCounty());
                    dto.setDistrict(record.getDistrict());
                    dto.setRegion(record.getRegion());
                    dto.setCoeficient(record.getCoeficient());
                    dto.setRegionId(record.getRegionId() != null ? record.getRegionId().toString() : null);
                    return dto;
                });

    }

    public List<PlaceDto> getPlaceByIds(List<UUID> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            return List.of(); // return empty list safely
        }

        return dsl.selectFrom(PLACE)
                .where(PLACE.ID.in(placeIds))
                .fetch()
                .map(record -> {
                    PlaceDto dto = new PlaceDto();
                    dto.setId(record.getId() != null ? record.getId().toString() : null);
                    dto.setName(record.getName());
                    dto.setAreaType(record.getAreaType());
                    dto.setCounty(record.getCounty());
                    dto.setDistrict(record.getDistrict());
                    dto.setRegion(record.getRegion());
                    dto.setCoeficient(record.getCoeficient());
                    dto.setRegionId(record.getRegionId() != null ? record.getRegionId().toString() : null);
                    return dto;
                });
    }


    public List<PlaceDto> getPlacesByIds(List<UUID> placeIds) {
        return dsl.selectFrom(PLACE)
                .where(PLACE.ID.in(placeIds))
                .fetch()
                .map(record -> PlaceDto.builder()
                        .name(record.getName())
                        .areaType(record.getAreaType())
                        .county(record.getCounty())
                        .district(record.getDistrict())
                        .region(record.getRegion()) // if applicable
                        .coeficient(record.getCoeficient()) // if applicable
                        .build());
    }

}
