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
public class ActiveOrderConversationDto {
    private Long orderId;
    private String guestName;
    private String location;
    private String orderStatus;
    private Double totalAmount;
    private String itemsSummary;
    private String latestMessageContent;
    private LocalDateTime latestMessageAt;
    private String latestMessageSenderRole;
}
