package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerConversationDto {
    private Long conversationId;
    private String guestName;
    private String guestEmail;
    private String propertyName;
    private String lastMessage;
    private String lastMessageAt;
    private long unreadCount;
    private String confirmationCode;
}
