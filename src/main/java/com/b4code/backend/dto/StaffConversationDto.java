package com.b4code.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffConversationDto {
    private Long staffId;
    private String staffName;
    private String staffRole;
    private Long propertyId;
    private String propertyName;
    private String latestMessageContent;
    private LocalDateTime latestMessageAt;
    private Integer unreadCount;
}
