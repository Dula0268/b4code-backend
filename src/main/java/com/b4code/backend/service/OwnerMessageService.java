package com.b4code.backend.service;

import com.b4code.backend.dto.owner.OwnerConversationDto;
import com.b4code.backend.dto.owner.OwnerMessageDto;

import java.util.List;

public interface OwnerMessageService {
    List<OwnerConversationDto> getConversations(String ownerEmail);
    List<OwnerMessageDto> getMessages(String ownerEmail, Long conversationId);
    OwnerMessageDto sendMessage(String ownerEmail, Long conversationId, String content);
    void markAsRead(String ownerEmail, Long conversationId);
}
