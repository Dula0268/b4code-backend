package com.b4code.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingMessageRequest {
    @NotBlank
    private String content;
}
