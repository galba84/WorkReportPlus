package com.example.workreportplus.service;

import com.example.jooq.tables.Descriptiontemplate;
import com.example.jooq.tables.Group;
import com.example.jooq.tables.records.DescriptiontemplateRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.jooq.Tables.DESCRIPTIONTEMPLATE;
import static com.example.workreportplus.Utils.SecurityUtil.getCurrentUsername;
import static com.example.workreportplus.service.GoogleSheetsService.DOVIDNYK_TABLE_ID;

@Service
public class DescriptionTemplateService {

    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public DescriptionTemplateService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    // Get all templates
    public List<DescriptiontemplateRecord> getAll() {
        return dsl.selectFrom(Descriptiontemplate.DESCRIPTIONTEMPLATE)
                .fetch();
    }

    // Get template by group name
    public Optional<DescriptiontemplateRecord> getByGroupName(String groupName) {
        return dsl.select(Descriptiontemplate.DESCRIPTIONTEMPLATE.fields())
                .from(Descriptiontemplate.DESCRIPTIONTEMPLATE)
                .join(Group.GROUP)
                .on(Descriptiontemplate.DESCRIPTIONTEMPLATE.GROUP_ID.eq(Group.GROUP.ID))
                .where(Group.GROUP.NAME.eq(groupName))
                .fetchOptionalInto(DescriptiontemplateRecord.class);
    }

    // Get template by group name
    public String getContentByGroupId(UUID groupId) {
        return dsl.select(Descriptiontemplate.DESCRIPTIONTEMPLATE.CONTENT)
                .from(Descriptiontemplate.DESCRIPTIONTEMPLATE)
                .join(Group.GROUP)
                .on(Descriptiontemplate.DESCRIPTIONTEMPLATE.GROUP_ID.eq(Group.GROUP.ID))
                .where(Group.GROUP.ID.eq(groupId))
                .limit(1)
                .fetchOneInto(String.class);
    }

    public String getContentRestByGroupId(UUID groupId) {
        return dsl.select(Descriptiontemplate.DESCRIPTIONTEMPLATE.CONTENT_REST)
                .from(Descriptiontemplate.DESCRIPTIONTEMPLATE)
                .join(Group.GROUP)
                .on(Descriptiontemplate.DESCRIPTIONTEMPLATE.GROUP_ID.eq(Group.GROUP.ID))
                .where(Group.GROUP.ID.eq(groupId))
                .limit(1)
                .fetchOneInto(String.class);
    }

    // Get template by group name
    public String getDetailsByGroupId(UUID groupId) {
        return dsl.select(Descriptiontemplate.DESCRIPTIONTEMPLATE.DETAILS)
                .from(Descriptiontemplate.DESCRIPTIONTEMPLATE)
                .join(Group.GROUP)
                .on(Descriptiontemplate.DESCRIPTIONTEMPLATE.GROUP_ID.eq(Group.GROUP.ID))
                .where(Group.GROUP.ID.eq(groupId))
                .limit(1)
                .fetchOneInto(String.class);
    }

    // Upsert (insert or update) template by group_id
    public void upsert(UUID groupId, String content, String username, String details) {
        dsl.insertInto(Descriptiontemplate.DESCRIPTIONTEMPLATE)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.GROUP_ID, groupId)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.CONTENT, content)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.DETAILS, details)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.CREATED_BY, username)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.UPDATED_BY, username)
                .onConflict(Descriptiontemplate.DESCRIPTIONTEMPLATE.GROUP_ID)
                .doUpdate()
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.CONTENT, content)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.UPDATED_BY, username)
                .set(Descriptiontemplate.DESCRIPTIONTEMPLATE.UPDATED_ON, OffsetDateTime.now())
                .execute();
    }

    public void updateFromTableSource() throws IOException {
        String range = "GroupReportText!A2:E"; // id, name

        List<List<Object>> rows = googleSheetsService.readSheet(DOVIDNYK_TABLE_ID, range);

        if (rows.isEmpty()) {
            System.out.println("No data found in RegionList sheet");
            return;
        }
        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            ctx.deleteFrom(DESCRIPTIONTEMPLATE).execute();

            ctx.batch(
                    rows.stream()
                            .filter(row -> row.size() >= 4) // minimal required columns
                            .filter(row -> !row.get(0).toString().isEmpty()) // minimal required columns
                            .filter(row -> !row.get(1).toString().isEmpty()) // minimal required columns
                            .filter(row -> !row.get(2).toString().isEmpty()) // minimal required columns
                            .filter(row -> !row.get(0).toString().equals("-"))
                            .map(row -> {
                                UUID groupId = !row.get(0).toString().isEmpty()
                                        ? UUID.fromString(row.get(0).toString())
                                        : UUID.randomUUID();

                                String description = row.size() > 2 ? row.get(2).toString().trim() : "";
                                String description_rest = row.size() >= 4 ? row.get(4).toString().trim() : "";
                                String details = row.size() > 3 ? row.get(3).toString().trim() : "";

                                return ctx.insertInto(DESCRIPTIONTEMPLATE)
                                        .set(DESCRIPTIONTEMPLATE.GROUP_ID, groupId)
                                        .set(DESCRIPTIONTEMPLATE.CONTENT, description)
                                        .set(DESCRIPTIONTEMPLATE.CONTENT_REST, description_rest)
                                        .set(DESCRIPTIONTEMPLATE.DETAILS, details)
                                        .set(DESCRIPTIONTEMPLATE.UPDATED_BY, getCurrentUsername())
                                        .set(DESCRIPTIONTEMPLATE.CREATED_BY, getCurrentUsername())
                                        .set(DESCRIPTIONTEMPLATE.CREATED_ON, OffsetDateTime.now())
                                        .set(DESCRIPTIONTEMPLATE.UPDATED_ON, OffsetDateTime.now())
                                        .onConflict(DESCRIPTIONTEMPLATE.GROUP_ID)
                                        .doUpdate()
                                        .set(DESCRIPTIONTEMPLATE.CONTENT, description)
                                        .set(DESCRIPTIONTEMPLATE.CONTENT_REST, description_rest);
                            })
                            .toList()
            ).execute();
        });
        System.out.println("Description updated successfully from sheet.");
    }
}
