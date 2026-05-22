package com.b4code.backend.service;

import com.b4code.backend.dto.AuditLogDto;
import com.b4code.backend.dto.AuditLogPageDto;

public interface AuditLogService {
    AuditLogPageDto getAuditLogs(String role, String search, int page, int size);
    
    void recordLog(Long userId, String userName, String userRole, String ipAddress, 
                   String action, String entity, String entityDetail);
}
