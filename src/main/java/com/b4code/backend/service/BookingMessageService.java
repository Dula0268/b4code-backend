package com.b4code.backend.service;

import com.b4code.backend.dto.ActiveConversationDto;
import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.BookingMessage;
import com.b4code.backend.repository.BookingMessageRepository;
import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.service.BookingSseService;
import com.b4code.backend.service.AutoReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingMessageService {

    private final BookingMessageRepository bookingMessageRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AutoReplyService autoReplyService;
    private final BookingSseService bookingSseService;

    @Transactional
    public BookingMessageDto sendMessage(String identifier, String senderEmail, String senderRole, String content) {
        Booking booking = findBookingByIdentifier(identifier);

        // Basic authorization validation could be done here or in controller
        if ("GUEST".equals(senderRole)) {
            if (senderEmail == null) {
                senderEmail = booking.getGuestEmail();
            } else if (!booking.getGuestEmail().equals(senderEmail)) {
                throw new RuntimeException("Unauthorized: Guest does not own this booking");
            }
        }

        BookingMessage message = BookingMessage.builder()
                .booking(booking)
                .senderEmail(senderEmail)
                .senderRole(senderRole)
                .content(content)
                .build();

        message = bookingMessageRepository.save(message);
        BookingMessageDto dto = mapToDto(message);

        // Broadcast to specific booking topics (both ID and confirmation code)
        messagingTemplate.convertAndSend("/topic/booking/" + booking.getId(), dto);
        if (booking.getConfirmationCode() != null) {
            messagingTemplate.convertAndSend("/topic/booking/" + booking.getConfirmationCode(), dto);
        }

        // Broadcast to property topic for staff (STOMP for actual message content)
        messagingTemplate.convertAndSend("/topic/property/" + booking.getProperty().getId() + "/messages", dto);
        
        // Also send SSE event for global unread badge on sidebar
        if ("GUEST".equals(senderRole)) {
            bookingSseService.sendPropertyEvent(booking.getProperty().getId(), "new-message", dto);
        }

        // Check for auto-replies if the message is from a GUEST
        if ("GUEST".equals(senderRole)) {
            autoReplyService.evaluateAndReply(booking, content);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<BookingMessageDto> getMessagesForBooking(String identifier) {
        Booking booking = findBookingByIdentifier(identifier);
        return bookingMessageRepository.findByBookingIdOrderByCreatedAtAsc(booking.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiveConversationDto> getConversationsForProperty(Long propertyId) {
        List<Long> bookingIds = bookingMessageRepository.findBookingIdsWithMessagesByPropertyId(propertyId);
        
        return bookingIds.stream().map(bookingId -> {
            Booking booking = bookingRepository.findById(bookingId).orElseThrow();
            List<BookingMessage> messages = bookingMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
            BookingMessage latestMessage = messages.get(messages.size() - 1);
            
            return ActiveConversationDto.builder()
                    .bookingId(booking.getId())
                    .confirmationCode(booking.getConfirmationCode())
                    .guestName(booking.getGuestName())
                    .propertyName(booking.getProperty().getName())
                    .roomName(booking.getRoomType() != null ? booking.getRoomType().getName() : null)
                    .checkIn(booking.getCheckIn())
                    .checkOut(booking.getCheckOut())
                    .latestMessageContent(latestMessage.getContent())
                    .latestMessageAt(latestMessage.getCreatedAt())
                    .latestMessageSenderRole(latestMessage.getSenderRole())
                    .build();
        }).collect(Collectors.toList());
    }

    private Booking findBookingByIdentifier(String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            return bookingRepository.findById(id).orElseGet(() ->
                    bookingRepository.findByConfirmationCode(identifier)
                            .orElseThrow(() -> new RuntimeException("Booking not found"))
            );
        } catch (NumberFormatException e) {
            return bookingRepository.findByConfirmationCode(identifier)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
        }
    }

    private BookingMessageDto mapToDto(BookingMessage message) {
        return BookingMessageDto.builder()
                .id(message.getId())
                .bookingId(message.getBooking().getId())
                .senderEmail(message.getSenderEmail())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
