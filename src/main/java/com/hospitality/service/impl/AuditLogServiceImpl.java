package com.hospitality.service.impl;

import com.hospitality.dao.AuditLogRepository;
import com.hospitality.dto.admin.AuditLogDto;
import com.hospitality.dto.admin.AuditLogPageDto;
import com.hospitality.models.AuditLog;
import com.hospitality.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AuditLogPageDto getAuditLogs(String role, String search, int page, int size) {
        String filterRole = (role != null && role.equalsIgnoreCase("All")) ? null : role;
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        
        Page<AuditLog> result = auditLogRepository.findAllWithFilters(
                filterRole, searchTerm,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        
        return AuditLogPageDto.builder()
                .content(result.map(AuditLogDto::fromEntity).toList())
                .currentPage(page)
                .totalPages(result.getTotalPages() == 0 ? 1 : result.getTotalPages())
                .totalElements(result.getTotalElements())
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional
    public void recordLog(Long userId, String userName, String userRole, String ipAddress, 
                          String action, String entity, String entityDetail) {
        AuditLog logEntry = new AuditLog();
        logEntry.setUserId(userId);
        logEntry.setUserName(userName);
        logEntry.setUserRole(userRole);
        logEntry.setIpAddress(ipAddress);
        logEntry.setAction(action);
        logEntry.setEntity(entity);
        logEntry.setEntityDetail(entityDetail);
        logEntry.setTimestamp(LocalDateTime.now());
        
        // Auto-assign avatar info based on name for mock display matching frontend
        logEntry.setAvatarInitial(userName != null && !userName.isEmpty() ? userName.substring(0, 1).toUpperCase() : "U");
        logEntry.setAvatarColor(assignColorByRole(userRole));
        
        auditLogRepository.save(logEntry);
        log.info("Audit log recorded: {} - {}", action, entity);
    }
    
    private String assignColorByRole(String role) {
        if (role == null) return "#95a5a6";
        return switch (role) {
            case "Admin" -> "#f4a261";
            case "Staff" -> "#2f80ed";
            case "Owner" -> "#27ae60";
            default -> "#95a5a6";
        };
    }
}
