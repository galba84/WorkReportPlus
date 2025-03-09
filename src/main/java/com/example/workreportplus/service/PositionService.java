package com.example.workreportplus.service;

import com.example.jooq.tables.Positions;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.jooq.Tables.POSITIONS;

@Service
public class PositionService {
    private final DSLContext dsl;

    public PositionService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public String getNameById(UUID positionId) {
        return dsl.selectFrom(Positions.POSITIONS)
                .where(POSITIONS.ID.eq(positionId))
                .fetchOne(POSITIONS.POSITION_NAME);

    }
}
