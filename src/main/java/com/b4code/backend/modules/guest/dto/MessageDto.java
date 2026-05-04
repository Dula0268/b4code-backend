package com.b4code.backend.modules.guest.dto;

import com.b4code.backend.modules.guest.entity.Message;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long propertyId;
    private String content;
    private String sentAt;

    public static MessageDto fromEntity(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .propertyId(message.getPropertyId())
                .content(message.getContent())
                .sentAt(message.getSentAt() != null ? message.getSentAt().toString() : null)
                .build();
    }
}
