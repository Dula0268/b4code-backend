package com.hospitality.service;

import com.hospitality.dto.admin.AuditLogDto;
import com.hospitality.dto.admin.AuditLogPageDto;

public interface AuditLogService {
    AuditLogPageDto getAuditLogs(String role, String search, int page, int size);
    
    void recordLog(Long userId, String userName, String userRole, String ipAddress, 
                   String action, String entity, String entityDetail);
}
