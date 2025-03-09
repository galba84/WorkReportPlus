package com.example.workreportplus.service;

import com.example.workreportplus.dto.PlaceDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.jooq.Tables.PLACE;

@Service
public class PlacesService {
    private final DSLContext dsl;

    public PlacesService(DSLContext dsl) {
        this.dsl = dsl;
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
}
