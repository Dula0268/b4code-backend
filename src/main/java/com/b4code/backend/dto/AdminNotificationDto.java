package com.b4code.backend.dto;

import com.b4code.backend.models.AdminNotification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminNotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private String referenceId;
    private boolean isRead;
    private String createdAt;

    public static AdminNotificationDto fromEntity(AdminNotification n) {
        return AdminNotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .referenceId(n.getReferenceId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null)
                .build();
    }
}
