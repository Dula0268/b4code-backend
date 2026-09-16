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
public class StaffQuickReplyDto {
    private Long id;
    private Long propertyId;
    private String name;
    private String message;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
