package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.models.Message.SenderType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {

    // ──────────────────────────────────
    // Send Message Request
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {

        @NotNull(message = "Booking ID is required")
        private Long bookingId;

        @NotNull(message = "Sender type is required")
        private SenderType senderType;

        @NotBlank(message = "Sender name is required")
        private String senderName;

        @NotBlank(message = "Message content is required")
        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        private String content;

        private String attachmentUrl;  // optional
    }

    // ──────────────────────────────────
    // Message Response
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageResponse {
        private Long id;
        private Long bookingId;
        private SenderType senderType;
        private String senderName;
        private String content;
        private String attachmentUrl;
        private Boolean isRead;
        private LocalDateTime sentAt;
    }

    // ──────────────────────────────────
    // Conversation (all messages for a booking)
    // ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversationResponse {
        private Long bookingId;
        private String confirmationNumber;
        private String propertyName;
        private String guestName;
        private List<MessageResponse> messages;
        private Integer unreadCount;
    }
}
