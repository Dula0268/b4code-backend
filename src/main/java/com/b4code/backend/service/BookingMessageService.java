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
    public BookingMessageDto sendMessage(String identifier, String senderEmail, String senderRole, String content, String targetRole) {
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
                .targetRole(targetRole != null ? targetRole : "STAFF")
                .content(content)
                .build();

        message = bookingMessageRepository.save(message);
        BookingMessageDto dto = mapToDto(message);

        // Broadcast to specific booking topics based on targetRole
        String topicSuffix = ("OWNER".equals(message.getTargetRole()) || "OWNER".equals(message.getSenderRole())) ? "/owner" : "";
        messagingTemplate.convertAndSend("/topic/booking/" + booking.getId() + topicSuffix, dto);
        if (booking.getConfirmationCode() != null) {
            messagingTemplate.convertAndSend("/topic/booking/" + booking.getConfirmationCode() + topicSuffix, dto);
        }

        // Broadcast to property topic for staff (STOMP for actual message content)
        if ("STAFF".equals(message.getTargetRole())) {
            messagingTemplate.convertAndSend("/topic/property/" + booking.getProperty().getId() + "/messages", dto);
        }
        
        if ("OWNER".equals(message.getTargetRole()) || "OWNER".equals(message.getSenderRole())) {
            messagingTemplate.convertAndSend("/topic/property/" + booking.getProperty().getId() + "/messages/owner", dto);
        }
        
        // Also send SSE event for global unread badge on sidebar
        if ("GUEST".equals(senderRole)) {
            bookingSseService.sendPropertyEvent(booking.getProperty().getId(), "new-message", dto);
        }

        // Check for auto-replies if the message is from a GUEST
        if ("GUEST".equals(senderRole)) {
            autoReplyService.evaluateAndReply(booking, content, targetRole);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<BookingMessageDto> getMessagesForBooking(String identifier, String targetRole) {
        Booking booking = findBookingByIdentifier(identifier);
        String finalTargetRole = targetRole != null ? targetRole : "STAFF";
        
        return bookingMessageRepository.findByBookingIdOrderByCreatedAtAsc(booking.getId())
                .stream()
                .filter(m -> 
                    (m.getSenderRole().equals("GUEST") && finalTargetRole.equals(m.getTargetRole())) ||
                    (m.getSenderRole().equals(finalTargetRole) && "GUEST".equals(m.getTargetRole()))
                )
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiveConversationDto> getConversationsForProperty(Long propertyId, String targetRole) {
        List<Long> bookingIds = bookingMessageRepository.findBookingIdsWithMessagesByPropertyId(propertyId);
        
        return bookingIds.stream().map(bookingId -> {
            List<BookingMessage> messages = bookingMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId)
                .stream()
                .filter(m -> targetRole.equals(m.getTargetRole()) || targetRole.equals(m.getSenderRole()))
                .collect(Collectors.toList());

            if (messages.isEmpty()) {
                return null;
            }

            Booking booking = bookingRepository.findById(bookingId).orElseThrow();
            BookingMessage latestMessage = messages.get(messages.size() - 1);
            
            String role = latestMessage.getSenderRole();
            if ("system@b4code.com".equals(latestMessage.getSenderEmail())) {
                role = "AUTO_REPLY";
            }

            return ActiveConversationDto.builder()
                    .bookingId(booking.getId())
                    .confirmationCode(booking.getConfirmationCode())
                    .guestName(booking.getGuestName())
                    .propertyId(booking.getProperty().getId())
                    .propertyName(booking.getProperty().getName())
                    .roomName(booking.getRoomType() != null ? booking.getRoomType().getName() : null)
                    .roomNumber(booking.getRoomNumber())
                    .checkIn(booking.getCheckIn())
                    .checkOut(booking.getCheckOut())
                    .latestMessageContent(latestMessage.getContent())
                    .latestMessageAt(latestMessage.getCreatedAt())
                    .latestMessageSenderRole(role)
                    .build();
        })
        .filter(dto -> dto != null)
        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiveConversationDto> getConversationsForOwner(String ownerEmail, String targetRole) {
        List<Long> bookingIds = bookingMessageRepository.findBookingIdsWithMessagesByOwnerEmail(ownerEmail);
        
        return bookingIds.stream().map(bookingId -> {
            List<BookingMessage> messages = bookingMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId)
                .stream()
                .filter(m -> targetRole.equals(m.getTargetRole()) || targetRole.equals(m.getSenderRole()))
                .collect(Collectors.toList());

            if (messages.isEmpty()) {
                return null;
            }

            Booking booking = bookingRepository.findById(bookingId).orElseThrow();
            BookingMessage latestMessage = messages.get(messages.size() - 1);
            
            String role = latestMessage.getSenderRole();
            if ("system@b4code.com".equals(latestMessage.getSenderEmail())) {
                role = "AUTO_REPLY";
            }

            return ActiveConversationDto.builder()
                    .bookingId(booking.getId())
                    .confirmationCode(booking.getConfirmationCode())
                    .guestName(booking.getGuestName())
                    .propertyId(booking.getProperty().getId())
                    .propertyName(booking.getProperty().getName())
                    .roomName(booking.getRoomType() != null ? booking.getRoomType().getName() : null)
                    .roomNumber(booking.getRoomNumber())
                    .checkIn(booking.getCheckIn())
                    .checkOut(booking.getCheckOut())
                    .latestMessageContent(latestMessage.getContent())
                    .latestMessageAt(latestMessage.getCreatedAt())
                    .latestMessageSenderRole(role)
                    .build();
        })
        .filter(dto -> dto != null)
        .collect(Collectors.toList());
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
                .targetRole(message.getTargetRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
