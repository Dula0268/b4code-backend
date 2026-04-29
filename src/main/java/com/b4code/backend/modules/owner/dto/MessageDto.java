package com.b4code.backend.modules.owner.dto;

import lombok.Data;
import java.util.List;

/**
 * DTOs for Message module
 */
public class MessageDto {

    @Data
    public static class ConversationResponse {
        private Long id;
        private String guestName;
        private String guestAvatar;
        private String lastMessage;
        private Boolean unread;
        private String propertyName;
        private String updatedAt;
    }

    @Data
    public static class ChatMessageResponse {
        private Long id;
        private Long senderId;
        private String senderType;
        private String content;
        private Boolean read;
        private String sentAt;
    }

    @Data
    public static class SendMessageRequest {
        private Long conversationId;
        private String content;
    }

    @Data
    public static class MessageOverviewResponse {
        private long unreadCount;
        private List<ConversationResponse> conversations;
    }
}
