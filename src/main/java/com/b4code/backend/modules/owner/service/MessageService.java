package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.MessageDto.*;
import com.b4code.backend.modules.owner.entity.*;
import com.b4code.backend.modules.owner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");

    public MessageOverviewResponse getConversations(Long ownerId) {
        List<Conversation> convos = conversationRepository.findByOwnerIdOrderByUpdatedAtDesc(ownerId);
        long unread = conversationRepository.countByOwnerIdAndUnread(ownerId, true);

        MessageOverviewResponse resp = new MessageOverviewResponse();
        resp.setUnreadCount(unread);
        resp.setConversations(convos.stream().map(this::toConvoResponse).collect(Collectors.toList()));
        return resp;
    }

    public List<ChatMessageResponse> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId).stream()
            .map(this::toMsgResponse).collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long ownerId, SendMessageRequest req) {
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(req.getConversationId());
        msg.setSenderId(ownerId);
        msg.setSenderType("OWNER");
        msg.setContent(req.getContent());
        msg.setRead(false);
        ChatMessage saved = messageRepository.save(msg);

        // Update conversation
        Conversation convo = conversationRepository.findById(req.getConversationId()).orElse(null);
        if (convo != null) { convo.setLastMessage(req.getContent()); conversationRepository.save(convo); }

        return toMsgResponse(saved);
    }

    @Transactional
    public void markAsRead(Long conversationId) {
        Conversation convo = conversationRepository.findById(conversationId).orElse(null);
        if (convo != null) { convo.setUnread(false); conversationRepository.save(convo); }
    }

    private ConversationResponse toConvoResponse(Conversation c) {
        ConversationResponse r = new ConversationResponse();
        r.setId(c.getId()); r.setGuestName(c.getGuestName()); r.setGuestAvatar(c.getGuestAvatar());
        r.setLastMessage(c.getLastMessage()); r.setUnread(c.getUnread()); r.setPropertyName(c.getPropertyName());
        r.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().format(FMT) : "");
        return r;
    }

    private ChatMessageResponse toMsgResponse(ChatMessage m) {
        ChatMessageResponse r = new ChatMessageResponse();
        r.setId(m.getId()); r.setSenderId(m.getSenderId()); r.setSenderType(m.getSenderType());
        r.setContent(m.getContent()); r.setRead(m.getRead());
        r.setSentAt(m.getSentAt() != null ? m.getSentAt().format(FMT) : "");
        return r;
    }
}
