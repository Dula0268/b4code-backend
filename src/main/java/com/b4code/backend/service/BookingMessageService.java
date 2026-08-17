package com.b4code.backend.service;

import com.b4code.backend.dto.ActiveConversationDto;
import com.b4code.backend.dto.BookingMessageDto;
import com.b4code.backend.models.Booking;
import com.b4code.backend.models.BookingMessage;
import com.b4code.backend.repository.BookingMessageRepository;
import com.b4code.backend.dao.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingMessageService {

    private final BookingMessageRepository bookingMessageRepository;
    private final BookingRepository bookingRepository;

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

        return mapToDto(message);
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
                    .latestMessageContent(latestMessage.getContent())
                    .latestMessageAt(latestMessage.getCreatedAt())
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
