package com.b4code.backend.modules.guest.controller;

import com.b4code.backend.modules.guest.dto.MessageResponse;
import com.b4code.backend.modules.guest.dto.SendMessageRequest;
import com.b4code.backend.modules.guest.entity.Message;
import com.b4code.backend.modules.guest.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Message saved = messageService.sendMessage(message);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/reply")
    public ResponseEntity<MessageResponse> sendMessageByEmail(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessageByEmail(request));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MessageResponse>> getMessagesByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(messageService.getMessagesByProperty(propertyId));
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<Message>> getMessagesForReceiver(@PathVariable Long receiverId) {
        return ResponseEntity.ok(messageService.getMessagesForReceiver(receiverId));
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Message>> getMessagesForSender(@PathVariable Long senderId) {
        return ResponseEntity.ok(messageService.getMessagesForSender(senderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        Optional<Message> message = messageService.getMessageById(id);
        return message.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}