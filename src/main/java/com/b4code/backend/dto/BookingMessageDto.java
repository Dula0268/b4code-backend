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
public class BookingMessageDto {
    private Long id;
    private Long bookingId;
    private String senderEmail;
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;
}
