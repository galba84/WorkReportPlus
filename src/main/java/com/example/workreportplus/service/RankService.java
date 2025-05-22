package com.example.workreportplus.service;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.jooq.Tables.RANK;

@Service
public class RankService {
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public RankService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public String getNameById(UUID id) {
        return dsl.selectFrom(RANK.RANK)
                .where(RANK.ID.eq(id))
                .fetchOne(RANK.NAME);

    }

    public void updateRanksFromTable() throws IOException {
        String range   = "Довідник!F2:F16";

        // 1) Pull the sheet data
        List<List<Object>> rows = googleSheetsService.readSheet(GoogleSheetsService.SHPS_TABLE_ID, range);
        if (rows.isEmpty()) {
            System.out.println("No data found in Places sheet");
            return;
        }

        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            Set<String> existingNames = ctx
                    .select(RANK.NAME)
                    .from(RANK)
                    .fetchSet(RANK.NAME);

            // `inserts` is inferred as List<InsertValuesStep2<RankRecord,UUID,String>>
            var inserts = rows.stream()
                    .filter(row -> row.size() >= 1)
                    .map(row -> row.get(0).toString().trim())
                    .filter(name -> !name.isBlank() && !existingNames.contains(name))
                    .map(name ->
                            ctx.insertInto(RANK)
                                    .columns(RANK.ID, RANK.NAME)
                                    .values(UUID.randomUUID(), name)
                    )
                    .toList();

            if (!inserts.isEmpty()) {
                ctx.batch(inserts).execute();
                System.out.printf("Inserted %d new ranks.%n", inserts.size());
            }
        });


        System.out.println("Ranks update complete.");
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
