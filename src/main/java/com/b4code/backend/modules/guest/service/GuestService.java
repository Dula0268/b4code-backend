package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.admin.dao.PropertyRepository;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.guest.dto.*;
import com.b4code.backend.modules.guest.entity.Booking;
import com.b4code.backend.modules.guest.entity.Message;
import com.b4code.backend.modules.guest.entity.Review;
import com.b4code.backend.modules.guest.repository.BookingRepository;
import com.b4code.backend.modules.guest.repository.MessageRepository;
import com.b4code.backend.modules.guest.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final com.b4code.backend.modules.guest.repository.RoomRepository roomRepository;

    // ───────── PROPERTY METHODS ─────────

    /**
     * Get all properties for guest search/browse
     */
    public List<PropertyListingDto> getAllProperties() {
        return propertyRepository.findAll()
                .stream()
                .map(PropertyListingDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get property details by ID
     */
    public PropertyDetailDto getPropertyDetail(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        PropertyDetailDto dto = PropertyDetailDto.fromEntity(property);
        
        // Fetch and add reviews
        List<Review> reviews = reviewRepository.findByPropertyId(propertyId);
        List<ReviewDetailDto> reviewDtos = reviews.stream()
                .map(ReviewDetailDto::fromEntity)
                .collect(Collectors.toList());
        dto.setReviews(reviewDtos);
        dto.setReviewCount(reviews.size());

        // Fetch and add rooms from DB if present
        try {
            List<com.b4code.backend.modules.guest.entity.Room> rooms = roomRepository.findByPropertyId(propertyId);
            List<RoomDto> roomDtos = rooms.stream()
                    .map(com.b4code.backend.modules.guest.dto.RoomDto::fromEntity)
                    .collect(Collectors.toList());
            dto.setRooms(roomDtos);
        } catch (Exception e) {
            // If rooms table doesn't exist or any error occurs, leave the sample rooms (if any) set in DTO
        }
        
        return dto;
    }

    // ───────── BOOKING METHODS ─────────

    /**
     * Get all bookings for a guest
     */
    public List<BookingDto> getGuestBookings(Long guestId) {
        return bookingRepository.findByGuestId(guestId)
                .stream()
                .map(BookingDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new booking
     */
    public BookingDto createBooking(Booking booking) {
        Booking savedBooking = bookingRepository.save(booking);
        return BookingDto.fromEntity(savedBooking);
    }

    /**
     * Get bookings for a property
     */
    public List<BookingDto> getPropertyBookings(Long propertyId) {
        return bookingRepository.findByPropertyId(propertyId)
                .stream()
                .map(BookingDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ───────── REVIEW METHODS ─────────

    /**
     * Get all reviews for a property
     */
    public List<ReviewDto> getPropertyReviews(Long propertyId) {
        return reviewRepository.findByPropertyId(propertyId)
                .stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new review
     */
    public ReviewDto createReview(Review review) {
        Review savedReview = reviewRepository.save(review);
        return ReviewDto.fromEntity(savedReview);
    }

    /**
     * Get reviews by guest
     */
    public List<ReviewDto> getGuestReviews(Long guestId) {
        return reviewRepository.findByGuestId(guestId)
                .stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ───────── MESSAGE METHODS ─────────

    /**
     * Get all messages for a property
     */
    public List<MessageDto> getPropertyMessages(Long propertyId) {
        return messageRepository.findByPropertyId(propertyId)
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get messages between two users
     */
    public List<MessageDto> getConversation(Long userId1, Long userId2) {
        return messageRepository.findBySenderIdAndReceiverId(userId1, userId2)
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Send a message
     */
    public MessageDto sendMessage(Message message) {
        Message savedMessage = messageRepository.save(message);
        return MessageDto.fromEntity(savedMessage);
    }

    /**
     * Get all messages received by a user
     */
    public List<MessageDto> getReceivedMessages(Long receiverId) {
        return messageRepository.findByReceiverId(receiverId)
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }
}
