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
public class OrderMessageDto {
    private Long id;
    private Long orderId;
    private String senderIdentifier;
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;
}
