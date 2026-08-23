package com.b4code.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveConversationDto {
    private Long bookingId;
    private String confirmationCode;
    private String guestName;
    private String propertyName;
    private String roomName;
    private String roomNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String latestMessageContent;
    private LocalDateTime latestMessageAt;
    private String latestMessageSenderRole;
}
