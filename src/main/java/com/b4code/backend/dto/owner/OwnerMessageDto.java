package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerMessageDto {
    private Long id;
    private Long conversationId;
    private String senderType;
    private String senderName;
    private String content;
    private String attachmentUrl;
    private Boolean isRead;
    private String sentAt;
}
