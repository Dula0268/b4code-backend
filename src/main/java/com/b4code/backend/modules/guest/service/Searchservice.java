package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.SearchDTO.*;
import com.b4code.backend.modules.guest.models.Property;
import com.b4code.backend.modules.guest.models.Room;
import com.b4code.backend.modules.guest.dao.BookingRepository;
import com.b4code.backend.modules.guest.dao.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    /**
     * Search for available properties matching the given criteria.
     * Returns results in under 2 seconds (per NFR requirement).
     */
    public List<PropertySearchResult> search(SearchRequest request) {

        // Default guests to 1 if not provided
        int guests = request.getGuests() != null ? request.getGuests() : 1;
        LocalDate checkIn  = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();

        List<Property> properties = propertyRepository.searchAvailableProperties(
            request.getDestination(),
            checkIn,
            checkOut,
            guests,
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getMinRating()
        );

        return properties.stream()
            .map(p -> mapToSearchResult(p, checkIn, checkOut, guests))
            .collect(Collectors.toList());
    }

    public PropertySearchResult getPropertyDetail(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new com.b4code.backend.modules.guest.exceptions.ResourceNotFoundException(
                "Property not found: " + propertyId));

        return mapToSearchResult(property, null, null, 1);
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    private PropertySearchResult mapToSearchResult(
            Property property, LocalDate checkIn, LocalDate checkOut, int guests) {

        List<Room> availableRooms = property.getRooms().stream()
            .filter(Room::getAvailable)
            .filter(r -> r.getMaxOccupancy() >= guests)
            .filter(r -> checkIn == null || checkOut == null || !bookingRepository.existsOverlappingBooking(r.getId(), checkIn, checkOut))
            .collect(Collectors.toList());

        var lowestPrice = availableRooms.stream()
            .map(Room::getPricePerNight)
            .min(java.math.BigDecimal::compareTo)
            .orElse(null);

        var roomSummaries = availableRooms.stream()
            .map(r -> RoomSummary.builder()
                .roomId(r.getId())
                .name(r.getName())
                .roomType(r.getRoomType())
                .maxOccupancy(r.getMaxOccupancy())
                .pricePerNight(r.getPricePerNight())
                .amenities(r.getAmenities())
                .build())
            .collect(Collectors.toList());

        return PropertySearchResult.builder()
            .propertyId(property.getId())
            .name(property.getName())
            .city(property.getCity())
            .address(property.getAddress())
            .latitude(property.getLatitude())
            .longitude(property.getLongitude())
            .averageRating(property.getAverageRating())
            .reviewCount(property.getReviewCount())
            .lowestPricePerNight(lowestPrice)
            .availableRooms(roomSummaries)
            .build();
    }
}