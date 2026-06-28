package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dao.MessageRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerConversationDto;
import com.b4code.backend.dto.owner.OwnerMessageDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.Message;
import com.b4code.backend.models.User;
import com.b4code.backend.service.OwnerMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerMessageServiceImpl implements OwnerMessageService {

    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OwnerConversationDto> getConversations(String ownerEmail) {
        User owner = resolveOwner(ownerEmail);
        List<Message> latest = messageRepository.findLatestMessagePerBookingByOwner(owner.getId());

        return latest.stream().map(m -> {
            Booking b = m.getBooking();
            long unread = messageRepository.countByBookingIdAndIsReadFalseAndSenderType(
                    b.getId(), Message.SenderType.GUEST);
            return OwnerConversationDto.builder()
                    .conversationId(b.getId())
                    .guestName(b.getGuestName())
                    .guestEmail(b.getGuestEmail())
                    .propertyName(b.getProperty() != null ? b.getProperty().getName() : null)
                    .lastMessage(m.getContent())
                    .lastMessageAt(m.getSentAt() != null ? m.getSentAt().toString() : null)
                    .unreadCount(unread)
                    .confirmationCode(b.getConfirmationCode())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerMessageDto> getMessages(String ownerEmail, Long conversationId) {
        resolveOwnedBooking(ownerEmail, conversationId);
        return messageRepository.findByBookingIdOrderBySentAtAsc(conversationId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public OwnerMessageDto sendMessage(String ownerEmail, Long conversationId, String content) {
        User owner = resolveOwner(ownerEmail);
        Booking booking = resolveOwnedBooking(ownerEmail, conversationId);

        Message msg = Message.builder()
                .booking(booking)
                .senderType(Message.SenderType.PROPERTY)
                .senderName(owner.getFullName() != null ? owner.getFullName() : ownerEmail)
                .content(content)
                .isRead(false)
                .build();
        return toDto(messageRepository.save(msg));
    }

    @Override
    @Transactional
    public void markAsRead(String ownerEmail, Long conversationId) {
        resolveOwnedBooking(ownerEmail, conversationId);
        List<Message> unread = messageRepository.findByBookingIdAndIsReadFalse(conversationId);
        unread.stream()
                .filter(m -> m.getSenderType() == Message.SenderType.GUEST)
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });
    }

    private OwnerMessageDto toDto(Message m) {
        return OwnerMessageDto.builder()
                .id(m.getId())
                .conversationId(m.getBooking() != null ? m.getBooking().getId() : null)
                .senderType(m.getSenderType() != null ? m.getSenderType().name() : null)
                .senderName(m.getSenderName())
                .content(m.getContent())
                .attachmentUrl(m.getAttachmentUrl())
                .isRead(m.getIsRead())
                .sentAt(m.getSentAt() != null ? m.getSentAt().toString() : null)
                .build();
    }

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
    }

    private Booking resolveOwnedBooking(String ownerEmail, Long bookingId) {
        User owner = resolveOwner(ownerEmail);
        return bookingRepository.findByIdAndPropertyOwnerId(bookingId, owner.getId())
                .orElseThrow(() -> new CustomException(
                        "Conversation not found or does not belong to this owner", HttpStatus.NOT_FOUND));
    }
}
