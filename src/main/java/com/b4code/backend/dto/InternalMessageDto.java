package com.b4code.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InternalMessageDto {
    private Long id;
    private Long propertyId;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
}
