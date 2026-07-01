package com.b4code.backend.rest;

import com.b4code.backend.dto.AuditLogPageDto;
import com.b4code.backend.service.AuditLogService;
import com.b4code.backend.service.AuditLogExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin — Audit Logs", description = "System audit logs and history")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogExportService auditLogExportService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List paginated audit logs with search and role filters")
    public ResponseEntity<AuditLogPageDto> getAuditLogs(
            @RequestParam(defaultValue = "All") String role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(role, search, page, size));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export audit logs as CSV")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(defaultValue = "All") String role,
            @RequestParam(required = false) String search) {
        
        byte[] csvData = auditLogExportService.exportToCsv(role, search);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-logs.csv\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvData);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export audit logs as PDF")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(defaultValue = "All") String role,
            @RequestParam(required = false) String search) {
        
        byte[] pdfData = auditLogExportService.exportToPdf(role, search);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-logs.pdf\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfData);
    }
}
