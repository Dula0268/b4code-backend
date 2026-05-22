package com.b4code.backend.service;

import com.b4code.backend.dto.MessageDTO.*;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Message;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;

    /**
     * Send a message in a booking conversation.
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking not found: " + request.getBookingId()));

        Message message = Message.builder()
            .booking(booking)
            .senderType(request.getSenderType())
            .senderName(request.getSenderName())
            .content(request.getContent())
            .attachmentUrl(request.getAttachmentUrl())
            .isRead(false)
            .build();

        Message saved = messageRepository.save(message);
        return mapToResponse(saved);
    }

    /**
     * Get full conversation for a booking.
     */
    public ConversationResponse getConversation(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking not found: " + bookingId));

        List<Message> messages = messageRepository.findByBookingIdOrderBySentAtAsc(bookingId);

        long unreadCount = messages.stream().filter(m -> !m.getIsRead()).count();

        List<MessageResponse> messageResponses = messages.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ConversationResponse.builder()
            .bookingId(bookingId)
            .confirmationNumber(booking.getConfirmationNumber())
            .propertyName(booking.getRoom().getProperty().getName())
            .guestName(booking.getGuestName())
            .messages(messageResponses)
            .unreadCount((int) unreadCount)
            .build();
    }

    /**
     * Mark messages as read.
     */
    @Transactional
    public void markAsRead(Long bookingId) {
        List<Message> unread = messageRepository.findByBookingIdAndIsReadFalse(bookingId);
        unread.forEach(m -> m.setIsRead(true));
        messageRepository.saveAll(unread);
    }

    // ──────────────────────────────────────────
    // Private helper
    // ──────────────────────────────────────────

    private MessageResponse mapToResponse(Message m) {
        return MessageResponse.builder()
            .id(m.getId())
            .bookingId(m.getBooking().getId())
            .senderType(m.getSenderType())
            .senderName(m.getSenderName())
            .content(m.getContent())
            .attachmentUrl(m.getAttachmentUrl())
            .isRead(m.getIsRead())
            .sentAt(m.getSentAt())
            .build();
    }
}
