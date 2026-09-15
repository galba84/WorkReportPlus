package com.example.workreportplus.controller;

import com.example.jooq.tables.records.AuditLogRecord;
import com.example.workreportplus.service.AuditLogService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-log")
@AllArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogDto> getAll() {
        return auditLogService
                .getAll(0, 1_000)
                .stream()
                .map(AuditLogDto::fromRecord)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDto> getById(@PathVariable UUID id) {
        Optional<AuditLogRecord> rec = auditLogService.getById(id);
        return rec
                .map(r -> ResponseEntity.ok(AuditLogDto.fromRecord(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> saveLog(@RequestBody CreateAuditLogDto req) {
        auditLogService.log(
                req.getAction(),
                req.getServiceId(),
                req.getEntityId(),
                req.getUserId(),
                req.getIpAddress(),
                req.getDetails()
        );
        return ResponseEntity.ok().build();
    }

    @Data
    @Builder
    public static class AuditLogDto {
        private UUID id;
        private String action;
        private String serviceId;
        private String entityId;
        private UUID userId;
        private String ipAddress;
        private String details;
        private String timestamp;

        static AuditLogDto fromRecord(AuditLogRecord r) {
            return AuditLogDto.builder()
                    .id(r.getId())
                    .action(r.getAction())
                    .serviceId(r.getServiceId())
                    .entityId(r.getEntityId())
                    .userId(r.getUserId())
                    .ipAddress(r.getIpAddress())
                    .details(r.getDetails())
                    .timestamp(r.getTimestamp()                     // java.sql.Timestamp
                            .toLocalDateTime()                  // → java.time.LocalDateTime
                            .atOffset(ZoneOffset.UTC)           // → java.time.OffsetDateTime
                            .toString())                 // ISO-86
                    .build();
        }
    }

    @Data
    public static class CreateAuditLogDto {
        private String action;
        private String serviceId;
        private String entityId;
        private UUID userId;
        private String ipAddress;
        private String details;
    }
}
