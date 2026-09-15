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
import static com.example.workreportplus.service.GoogleSheetsService.DOVIDNYK_TABLE_ID;

@Service
public class GroupService {

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
                .and(GROUP.STATUS.isTrue())
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

    public List<GroupDto> getAllGroupsAnyStatus() {
        return dsl.selectFrom(GROUP)
                .fetchInto(GroupDto.class);
    }

    public GroupDto getGroupById(UUID groupId) {
        return dsl.selectFrom(GROUP)
                .where(GROUP.STATUS.isTrue())
                .and(GROUP.ID.eq(groupId))
                .fetchOneInto(GroupDto.class);
    }

    public List<UUID> getAllGroupIds() {
        return dsl.select(GROUP.ID)
                .from(GROUP)
                .fetchInto(UUID.class);
    }

    public List<UUID> getAllGroupIdsValid() {
        return dsl.select(GROUP.ID)
                .from(GROUP)
                .where(GROUP.STATUS.isTrue())
                .fetchInto(UUID.class);
    }

    public List<String> getGroupNames() {
        return dsl.select(GROUP.NAME)
                .from(GROUP)
                .where(GROUP.STATUS.isTrue())
                .fetchInto(String.class);
    }

    public List<UUID> getGroupIdsByRegionId(UUID regionId) {
        return dsl.select(GROUP.ID)
                .from(GROUP)
                .where(GROUP.REGION_ID.eq(regionId))
                .and(GROUP.STATUS.isTrue())
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

    public void updateGroupsFromTable() throws IOException {
        String range = "GroupList!A2:E"; // assuming A = id, B = name, C = region, D = regionId
        List<List<Object>> rows = googleSheetsService.readSheet(DOVIDNYK_TABLE_ID, range);
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
                                    .filter(row -> row.size() >= 5) // minimal required columns
                                    .filter(row -> !row.get(0).toString().isEmpty())
                                    .filter(row -> !row.get(1).toString().isEmpty())
                                    .filter(row -> !row.get(2).toString().isEmpty())
                                    .filter(row -> !row.get(3).toString().isEmpty())
                                    .filter(row -> !row.get(4).toString().isEmpty())
                                    .map(row -> {
                                        UUID groupId = !row.get(0).toString().isEmpty() ? UUID.fromString(row.get(0).toString()) : UUID.randomUUID();
                                        String groupName = row.get(1).toString().trim();
                                        UUID regionId = UUID.fromString(row.get(3).toString());
                                        boolean isFighting = "Б".equalsIgnoreCase(row.get(4).toString().trim());
                                        return ctx.insertInto(GROUP)
                                                .set(GROUP.ID, groupId)
                                                .set(GROUP.NAME, groupName)
                                                .set(GROUP.REGION_ID, regionId)
                                                .set(GROUP.STATUS, true)
                                                .set(GROUP.IS_FIGHTING, isFighting)
                                                .onConflict(GROUP.ID)
                                                .doUpdate()
                                                .set(GROUP.NAME, groupName)
                                                .set(GROUP.REGION_ID, regionId)
                                                .set(GROUP.STATUS, true)
                                                .set(GROUP.IS_FIGHTING, isFighting);
                                    })
                                    .toList()
                    ).execute();
                });
        System.out.println("Groups updated successfully from sheet.");
    }

    public void insertGroup(GroupDto dto) {
        dsl.insertInto(GROUP)
                .set(GROUP.ID, dto.getId() != null ? UUID.fromString(dto.getId()) : UUID.randomUUID())
                .set(GROUP.NAME, dto.getName())
                .set(GROUP.REGION_ID, dto.getRegionId())
                .set(GROUP.STATUS, true)
                .set(GROUP.IS_FIGHTING, dto.isFighting())
                .onConflict(GROUP.ID)
                .doNothing()
                .execute();
    }

    public void updateGroup(UUID id, GroupDto dto) {
        dsl.update(GROUP)
                .set(GROUP.NAME, dto.getName())
                .set(GROUP.REGION_ID, dto.getRegionId())
                .set(GROUP.IS_FIGHTING, dto.isFighting())
                .where(GROUP.ID.eq(id))
                .execute();
    }

    public void updateGroupStatus(UUID id, boolean active) {
        dsl.update(GROUP)
                .set(GROUP.STATUS, active)
                .where(GROUP.ID.eq(id))
                .execute();
    }



}
