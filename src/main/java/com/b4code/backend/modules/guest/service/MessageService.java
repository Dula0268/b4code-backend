package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import com.b4code.backend.modules.guest.dto.MessageResponse;
import com.b4code.backend.modules.guest.dto.SendMessageRequest;
import com.b4code.backend.modules.guest.entity.Message;
import com.b4code.backend.modules.guest.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public Message sendMessage(Message message) {
        return messageRepository.save(message);
    }

    public MessageResponse sendMessageByEmail(SendMessageRequest request) {
        User sender = userRepository.findByEmail(request.getSenderEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByEmail(request.getReceiverEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setSenderId(sender.getId());
        message.setReceiverId(receiver.getId());
        message.setPropertyId(request.getPropertyId());
        message.setContent(request.getContent());

        Message saved = messageRepository.save(message);
        return toResponse(saved);
    }

    public List<Message> getMessagesForReceiver(Long receiverId) {
        return messageRepository.findByReceiverIdOrderBySentAtDesc(receiverId);
    }

    public List<Message> getMessagesForSender(Long senderId) {
        return messageRepository.findBySenderIdOrderBySentAtDesc(senderId);
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public List<MessageResponse> getMessagesByProperty(Long propertyId) {
        return messageRepository.findByPropertyIdOrderBySentAtDesc(propertyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    private MessageResponse toResponse(Message message) {
        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
        return new MessageResponse(
                message.getId(),
                message.getPropertyId(),
                message.getSenderId(),
                sender != null ? sender.getEmail() : null,
                sender != null ? sender.getFirstName() + " " + sender.getLastName() : null,
                message.getReceiverId(),
                receiver != null ? receiver.getEmail() : null,
                receiver != null ? receiver.getFirstName() + " " + receiver.getLastName() : null,
                message.getContent(),
                message.getSentAt()
        );
    }
}