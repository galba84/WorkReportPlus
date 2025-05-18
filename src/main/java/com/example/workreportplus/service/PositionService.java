package com.example.workreportplus.service;

import com.example.jooq.tables.Positions;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.jooq.Tables.POSITIONS;

@Service
public class PositionService {
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public PositionService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public String getNameById(UUID positionId) {
        return dsl.selectFrom(Positions.POSITIONS)
                .where(POSITIONS.ID.eq(positionId))
                .fetchOne(POSITIONS.POSITION_NAME);
    }

    public void updatePositionsFromTable() throws IOException {
        String range = "Штатка!S2:S648"; // Only column S is needed

        List<List<Object>> rows = googleSheetsService.readSheet(GoogleSheetsService.SHPS_TABLE_ID, range);
        if (rows.isEmpty()) {
            System.out.println("No data found in Positions sheet; skipping update.");
            return;
        }

        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            Set<String> existingNames = ctx
                    .select(POSITIONS.POSITION_NAME)
                    .from(POSITIONS)
                    .fetchSet(POSITIONS.POSITION_NAME);

            Set<String> newNames = rows.stream()
                    .map(row -> row.get(0).toString().trim())
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.toSet());

            Set<String> missingNames = newNames.stream()
                    .filter(name -> !existingNames.contains(name))
                    .collect(Collectors.toSet());

            List<Query> inserts = missingNames.stream()
                    .map(name -> (Query) ctx.insertInto(POSITIONS)
                            .columns(POSITIONS.ID, POSITIONS.POSITION_NAME)
                            .values(UUID.randomUUID(), name))
                    .toList();


            if (!inserts.isEmpty()) {
                ctx.batch(inserts).execute();
                System.out.printf("✅ Inserted %d new positions.%n", inserts.size());
            } else {
                System.out.println("All positions are already up to date; no inserts needed.");
            }
        });

        System.out.println("✅ Positions update complete.");
    }


}
