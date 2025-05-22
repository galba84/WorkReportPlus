package com.example.workreportplus.service;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.example.jooq.Tables.RANK;
import static com.example.jooq.Tables.UNIT;
import static com.example.workreportplus.service.GoogleSheetsService.DOVIDNYK_TABLE_ID;

@Service
public class UnitService {
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public UnitService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public String getNameById(UUID id) {
        return dsl.selectFrom(RANK.RANK)
                .where(RANK.ID.eq(id))
                .fetchOne(RANK.NAME);

    }

    public void updateUnitsFromTable() throws IOException {
        String range = "UnitList!A2:B"; // adjust if needed


        // 1) Pull the sheet data
        List<List<Object>> rows = googleSheetsService.readSheet(DOVIDNYK_TABLE_ID, range);
        if (rows.isEmpty()) {
            System.out.println("No data found in UnitList sheet");
            return;
        }

        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            // 1) load all existing IDs
            Set<UUID> existingIds = ctx
                    .select(UNIT.ID)
                    .from(UNIT)
                    .fetchSet(UNIT.ID);

            // 2) batch only truly new ones
            var inserts = rows.stream()
                    .filter(row -> row.size() >= 2)
                    .map(row -> {
                        UUID id   = UUID.fromString(row.get(0).toString().trim());
                        String name = row.get(1).toString().trim();
                        return Map.entry(id, name);
                    })
                    .filter(e -> !e.getValue().isBlank() && !existingIds.contains(e.getKey()))
                    .map(e ->
                            ctx.insertInto(UNIT)
                                    .columns(UNIT.ID, UNIT.UNIT_NAME)
                                    .values(e.getKey(), e.getValue())
                    )
                    .toList();

            if (!inserts.isEmpty()) {
                ctx.batch(inserts).execute();
                System.out.printf("Inserted %d new units.%n", inserts.size());
            } else {
                System.out.println("No new units to insert.");
            }
        });



        System.out.println("Units update complete.");
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
}
