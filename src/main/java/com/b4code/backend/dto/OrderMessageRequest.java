package com.b4code.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderMessageRequest {
    @NotBlank
    private String content;
}
