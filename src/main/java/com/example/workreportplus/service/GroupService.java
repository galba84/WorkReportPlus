package com.example.workreportplus.service;

import com.example.workreportplus.dto.GroupDto;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.example.jooq.tables.Group.GROUP;

@Service
public class GroupService {

    public static final String SHEEDT_ID = "1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss";
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;

    public GroupService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public UUID getGroupIdByName(String groupName) {
        return dsl.select(GROUP.ID)
                .from(GROUP)
                .where(GROUP.NAME.eq(groupName))
                .fetchOne(GROUP.ID);
    }

    public boolean groupExistsByName(String groupName) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(GROUP)
                        .where(GROUP.NAME.eq(groupName))
        );
    }


    public List<GroupDto> getAllGroups() {
        return dsl.selectFrom(GROUP)
                .where(GROUP.STATUS.isTrue())
                .fetchInto(GroupDto.class);
    }

    public List<UUID> getAllGroupIds() {
        return dsl.select(GROUP.ID)
                .from(GROUP)
                .fetchInto(UUID.class);
    }

    public List<String> getGroupNames() {
        return dsl.select(GROUP.NAME)
                .from(GROUP)
                .fetchInto(String.class);
    }

    public List<UUID> getGroupIdsByRegionId(UUID regionId) {
        return dsl.select(GROUP.ID)
                .from(GROUP)
                .where(GROUP.REGION_ID.eq(regionId))
                .fetchInto(UUID.class);
    }

    public UUID getRegionIdByGroupId(UUID groupId) {
        Record1<UUID> result = dsl.select(GROUP.REGION_ID)
                .from(GROUP)
                .where(GROUP.ID.eq(groupId))
                .fetchOne();
        return result != null ? result.value1() : null;
    }


    public String getGroupNameById(String groupId) {
        return dsl.select(GROUP.NAME)
                .from(GROUP)
                .where(GROUP.ID.eq(UUID.fromString(groupId)))
                .fetchOne(GROUP.NAME);
    }

    public String getGroupNameById(UUID groupId) {
        return dsl.select(GROUP.NAME)
                .from(GROUP)
                .where(GROUP.ID.eq(groupId))
                .fetchOne(GROUP.NAME);
    }

    public UUID upsertGroup(UUID id, String groupName, UUID regionId) {
        UUID existingId = dsl.select(GROUP.ID)
                .from(GROUP)
                .where(GROUP.NAME.eq(groupName))
                .fetchOne(GROUP.ID);

        if (existingId != null) {
            return existingId;
        }

        // generate id if null
        UUID finalId = (id != null) ? id : UUID.randomUUID();

        dsl.insertInto(GROUP)
                .set(GROUP.ID, finalId)
                .set(GROUP.NAME, groupName)
                .set(GROUP.REGION_ID, regionId)
                .execute();

        return finalId;
    }


    public void updateGroupsFromTable() throws IOException {
        String sheetId = SHEEDT_ID; // or make it configurable
        String range = "GroupList!A2:D"; // assuming A = id, B = name, C = region, D = regionId

        List<List<Object>> rows = googleSheetsService.readSheet(sheetId, range);

        if (rows.isEmpty()) {
            System.out.println("No data found in GroupList sheet");
            return;
        }
        dsl.transaction(configuration -> {
                    DSLContext ctx = DSL.using(configuration);

                    ctx.update(GROUP)
                            .set(GROUP.STATUS, false)
                            .execute();
                    ctx.batch(
                            rows.stream()
                                    .filter(row -> row.size() >= 4) // minimal required columns
                                    .filter(row -> !row.get(0).toString().isEmpty())
                                    .filter(row -> !row.get(1).toString().isEmpty())
                                    .filter(row -> !row.get(2).toString().isEmpty())
                                    .filter(row -> !row.get(3).toString().isEmpty())

                                    .map(row -> {
                                        UUID groupId = !row.get(0).toString().isEmpty() ? UUID.fromString(row.get(0).toString()) : UUID.randomUUID();
                                        String groupName = row.get(1).toString().trim();
                                        UUID regionId = UUID.fromString(row.get(3).toString());

                                        return ctx.insertInto(GROUP)
                                                .set(GROUP.ID, groupId)
                                                .set(GROUP.NAME, groupName)
                                                .set(GROUP.REGION_ID, regionId)
                                                .set(GROUP.STATUS, true)
                                                .onConflict(GROUP.ID)
                                                .doUpdate()
                                                .set(GROUP.NAME, groupName)
                                                .set(GROUP.REGION_ID, regionId)
                                                .set(GROUP.STATUS, true);
                                    })
                                    .toList()
                    ).execute();
                });
        System.out.println("Groups updated successfully from sheet.");
    }


}
