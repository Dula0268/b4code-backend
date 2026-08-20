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
public class AutoReplyRuleDto {
    private Long id;
    private Long propertyId;
    private String keyword;
    private String replyMessage;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
