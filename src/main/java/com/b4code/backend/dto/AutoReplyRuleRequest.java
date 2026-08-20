package com.b4code.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutoReplyRuleRequest {
    
    @NotBlank(message = "Keyword is required")
    private String keyword;

    @NotBlank(message = "Reply message is required")
    private String replyMessage;

    @NotNull
    private Boolean isActive = true;
}
