package com.example.workreportplus.controller;

import com.example.jooq.tables.records.AuditLogRecord;
import com.example.workreportplus.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/audit-log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogRecord>> getAll() {
        List<AuditLogRecord> logs = auditLogService.getAll(0,1000);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/admin")
    public String viewAuditLog(Model model) {
        List<AuditLogRecord> auditLogs = auditLogService.getAll(0, 1000);
        model.addAttribute("auditLogs", auditLogs);
        return "audit_log"; // resolves to templates/audit_log.html
    }



    @GetMapping("/{id}")
    public ResponseEntity<AuditLogRecord> getById(@PathVariable UUID id) {
        Optional<AuditLogRecord> record = auditLogService.getById(id);
        return record.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> saveReport(
            @RequestParam String action,
            @RequestParam String serviceId,
            @RequestParam String entityId,
            @RequestParam(required = false) UUID userId,
            @RequestParam String ipAddress,
            @RequestParam(required = false) String details
    ) {
        auditLogService.log(action, serviceId, entityId, userId, ipAddress, details);
        return ResponseEntity.ok().build();
    }
}
