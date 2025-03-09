package com.example.workreportplus.service;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

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
}
