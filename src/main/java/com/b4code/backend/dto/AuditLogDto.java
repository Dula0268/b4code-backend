package com.b4code.backend.dto;

import com.b4code.backend.models.AuditLog;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogDto {

    private String id;
    private String userName;
    private String userRole;      
    private String avatarColor;
    private String avatarInitial;
    private String ip;              
    private String action;
    private String entity;
    private String entityDetail;
    private String timestamp;       

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public static AuditLogDto fromEntity(AuditLog log) {
        return AuditLogDto.builder()
                .id(String.valueOf(log.getId()))
                .userName(log.getUser() != null ? log.getUser().getFullName() : null)
                .userRole(log.getUser() != null && log.getUser().getRole() != null ? log.getUser().getRole().name() : null)
                .avatarColor(null)
                .avatarInitial(log.getUser() != null && log.getUser().getFirstName() != null && !log.getUser().getFirstName().isEmpty() ? log.getUser().getFirstName().substring(0, 1) : null)
                .ip(log.getIpAddress())
                .action(log.getAction())
                .entity(log.getEntity())
                .entityDetail(log.getEntityDetail())
                .timestamp(log.getTimestamp() != null ? log.getTimestamp().format(FMT) : "")
                .build();
    }
}

