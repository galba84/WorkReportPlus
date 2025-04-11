package com.example.workreportplus.service;

import com.example.jooq.tables.Descriptiontemplate;
import com.example.jooq.tables.Group;
import com.example.jooq.tables.records.DescriptiontemplateRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.jooq.Tables.DESCRIPTIONTEMPLATE;
import static com.example.workreportplus.Utils.SecurityUtil.getCurrentUsername;

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

    public  void updateFromTableSource() throws IOException {
        String sheetId = "1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss"; // or inject as a property
        String range = "GroupReportText!A2:D"; // id, name

        List<List<Object>> rows = googleSheetsService.readSheet(sheetId, range);

        if (rows.isEmpty()) {
            System.out.println("No data found in RegionList sheet");
            return;
        }

        dsl.deleteFrom(DESCRIPTIONTEMPLATE).execute();

        dsl.batch(
                rows.stream()
                        .filter(row -> row.size() >= 2) // minimal required columns
                        .map(row -> {
                            UUID groupId = !row.get(0).toString().isEmpty()
                                    ? UUID.fromString(row.get(0).toString())
                                    : UUID.randomUUID();

                            String description = row.size() > 2 ? row.get(2).toString().trim() : "";
                            String details = row.size() > 3 ? row.get(3).toString().trim() : "";

                            return dsl.insertInto(DESCRIPTIONTEMPLATE)
                                    .set(DESCRIPTIONTEMPLATE.GROUP_ID, groupId)
                                    .set(DESCRIPTIONTEMPLATE.CONTENT, description)
                                    .set(DESCRIPTIONTEMPLATE.DETAILS, details)
                                    .set(DESCRIPTIONTEMPLATE.UPDATED_BY, getCurrentUsername())
                                    .set(DESCRIPTIONTEMPLATE.CREATED_BY, getCurrentUsername())
                                    .set(DESCRIPTIONTEMPLATE.CREATED_ON, OffsetDateTime.now())
                                    .set(DESCRIPTIONTEMPLATE.UPDATED_ON, OffsetDateTime.now())
                                    .onConflict(DESCRIPTIONTEMPLATE.GROUP_ID)
                                    .doUpdate()
                                    .set(DESCRIPTIONTEMPLATE.CONTENT, description);
                        })
                        .toList()
        ).execute();

        System.out.println("Description updated successfully from sheet.");
    }
}
