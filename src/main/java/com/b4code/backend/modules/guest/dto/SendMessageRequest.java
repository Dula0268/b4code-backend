package com.b4code.backend.modules.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank
    private String senderEmail;

    @NotBlank
    private String receiverEmail;

    @NotNull
    private Long propertyId;

    @NotBlank
    private String content;
}