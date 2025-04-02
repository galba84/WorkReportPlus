package com.example.workreportplus.service;

import com.example.workreportplus.dto.GroupDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.jooq.tables.Group.GROUP;

@Service
public class GroupService {

    private final DSLContext dsl;

    public GroupService(DSLContext dsl) {
        this.dsl = dsl;
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
                .fetchInto(GroupDto.class);
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

    public String getGroupNameById(String groupId) {
        return dsl.select(GROUP.NAME)
                .from(GROUP)
                .where(GROUP.ID.eq(UUID.fromString(groupId)))
                .fetchOne(GROUP.NAME);
    }
}
