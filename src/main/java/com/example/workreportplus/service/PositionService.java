package com.example.workreportplus.service;

import com.example.jooq.tables.Positions;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        String sheetId = "1LkkLuk7y_fB8BTa-T4kYPYTgT-cVhIIgJTlZyo2b0UA";
        String range   = "Відмінки Посад!A2:A648";

        // 1) Read the sheet
        List<List<Object>> rows = googleSheetsService.readSheet(sheetId, range);
        if (rows.isEmpty()) {
            System.out.println("No data found in Positions sheet; skipping update.");
            return;
        }

        // 2) Do everything in a transaction
        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            // Load existing names into a Set for quick lookup
            Set<String> existingNames = ctx
                    .select(POSITIONS.POSITION_NAME)
                    .from(POSITIONS)
                    .fetchSet(POSITIONS.POSITION_NAME);

            // Prepare only those INSERTs whose names aren’t in existingNames
            var inserts = rows.stream()
                    .map(row -> row.get(0).toString().trim())
                    .filter(name -> !name.isBlank())               // skip blanks
                    .filter(name -> !existingNames.contains(name)) // only new names
                    .map(name ->
                            ctx.insertInto(POSITIONS)
                                    .columns(POSITIONS.ID, POSITIONS.POSITION_NAME)
                                    .values(UUID.randomUUID(), name)
                    )
                    .toList();

            if (!inserts.isEmpty()) {
                ctx.batch(inserts).execute();
                System.out.printf("Inserted %d new positions.%n", inserts.size());
            } else {
                System.out.println("All positions are already up to date; no inserts needed.");
            }
        });

        System.out.println("Positions update complete.");
    }

}
