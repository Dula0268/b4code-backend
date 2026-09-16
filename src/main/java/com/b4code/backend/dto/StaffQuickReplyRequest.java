package com.b4code.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffQuickReplyRequest {
    
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Message cannot be empty")
    private String message;
    
    @NotNull(message = "isActive flag is required")
    private Boolean isActive;
}
