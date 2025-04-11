package com.example.workreportplus.service;

import com.example.jooq.tables.records.AuditLogRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.jooq.tables.AuditLog.AUDIT_LOG;

@Service
public class AuditLogService {

    private final DSLContext dsl;

    public AuditLogService(DSLContext dsl) {
        this.dsl = dsl;
    }



    public AuditLogRecord log(
            String action,
            String serviceId,
            String entityId,
            UUID userId,
            String ipAddress,
            String details
    ) {
        UUID id = UUID.randomUUID();

        Optional<String> optionalIp = Optional.ofNullable(ipAddress)
                .filter(ip -> !ip.isBlank())
                .map(ip -> ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip);

        dsl.insertInto(AUDIT_LOG)
                .set(AUDIT_LOG.ID, id)
                .set(AUDIT_LOG.ACTION, action)
                .set(AUDIT_LOG.SERVICE_ID, serviceId)
                .set(AUDIT_LOG.ENTITY_ID, entityId)
                .set(AUDIT_LOG.USER_ID, userId)
                // ✅ Use raw SQL with bind placeholder and casting
                .set(AUDIT_LOG.IP_ADDRESS, DSL.val(optionalIp, SQLDataType.VARCHAR))
                .execute();
        return dsl.selectFrom(AUDIT_LOG)
                .where(AUDIT_LOG.ID.eq(id))
                .fetchOne();
    }


    public String normalizeToIPv4(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "127.0.0.1";
        }

        // IPv6 loopback to IPv4 loopback
        if ("::1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        // Otherwise return original
        return ipAddress;
    }


    public List<AuditLogRecord> getAll(int offset, int limit) {
        return dsl.selectFrom(AUDIT_LOG)
                .orderBy(AUDIT_LOG.TIMESTAMP.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }


    public Optional<AuditLogRecord> getById(UUID id) {
        return Optional.ofNullable(
                dsl.selectFrom(AUDIT_LOG)
                        .where(AUDIT_LOG.ID.eq(id))
                        .fetchOne()
        );
    }
}
