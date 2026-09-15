package com.b4code.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InternalMessageRequest {
    @NotBlank(message = "Message content is required")
    private String content;
}
