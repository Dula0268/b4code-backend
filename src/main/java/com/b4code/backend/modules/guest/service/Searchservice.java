package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.SearchDTO.*;
import com.b4code.backend.modules.guest.models.Property;
import com.b4code.backend.modules.guest.models.Room;
import com.b4code.backend.modules.guest.dao.BookingRepository;
import com.b4code.backend.modules.guest.dao.PropertyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    public SearchService(
            @Qualifier("guestPropertyRepository") PropertyRepository propertyRepository,
            BookingRepository bookingRepository) {
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<PropertySearchResult> search(SearchRequest request) {
        int guests = request.getGuests() != null ? request.getGuests() : 1;
        LocalDate checkIn = request.getCheckIn();
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
            .map(p -> mapToPropertySearchResult(p, checkIn, checkOut, guests))
            .collect(Collectors.toList());
    }

    public PropertyDetailResult getPropertyDetail(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new com.b4code.backend.modules.guest.exceptions.ResourceNotFoundException(
                "Property not found: " + propertyId));

        return mapToPropertyDetailResult(property);
    }

    private PropertySearchResult mapToPropertySearchResult(
            Property property, LocalDate checkIn, LocalDate checkOut, int guests) {

        List<Room> availableRooms = property.getRooms().stream()
            .filter(Room::getAvailable)
            .filter(r -> r.getMaxOccupancy() >= guests)
            .filter(r -> checkIn == null || checkOut == null || !bookingRepository.existsOverlappingBooking(r.getId(), checkIn, checkOut))
            .collect(Collectors.toList());

        BigDecimal lowestPrice = availableRooms.stream()
            .map(Room::getPricePerNight)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        int maxGuests = availableRooms.stream()
            .mapToInt(Room::getMaxOccupancy)
            .max()
            .orElse(property.getBaseGuests());

        return PropertySearchResult.builder()
            .id(property.getId())
            .title(property.getName())
            .location(property.getCity())
            .propertyType(property.getPropertyType())
            .pricePerNight(lowestPrice)
            .maxGuests(maxGuests)
            .baseGuests(property.getBaseGuests())
            .extraGuestFee(property.getExtraGuestFee())
            .rating(property.getAverageRating() != null ? property.getAverageRating() : 0.0)
            .reviewCount(property.getReviewCount() != null ? property.getReviewCount() : 0)
            .badge(property.getBadge())
            .imageSrc(property.getImageSrc())
            .build();
    }

    private PropertyDetailResult mapToPropertyDetailResult(Property property) {
        List<String> galleryImages = property.getGalleryImages() != null && !property.getGalleryImages().isEmpty()
            ? Arrays.asList(property.getGalleryImages().split(","))
            : new ArrayList<>();
            
        List<AmenityDTO> amenitiesList = new ArrayList<>();
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            String[] ams = property.getAmenities().split(",");
            for (String am : ams) {
                String[] parts = am.split(":"); // Expecting "IconName:Label"
                if (parts.length == 2) {
                    amenitiesList.add(new AmenityDTO(parts[0], parts[1]));
                } else {
                    amenitiesList.add(new AmenityDTO("Check", parts[0])); // Fallback icon
                }
            }
        }
        
        // Mock reviews for now as they might not be deeply seeded in the exact format needed. 
        // We can leave empty, or create mock ReviewDTOs based on the property's DB reviews if available.
        // Let's keep it simple and just map the rooms properly.

        List<RoomDTO> roomDTOs = property.getRooms().stream().map(r -> {
            List<String> features = r.getFeatures() != null && !r.getFeatures().isEmpty()
                ? Arrays.asList(r.getFeatures().split(","))
                : new ArrayList<>();
                
            return RoomDTO.builder()
                .id(r.getId().toString())
                .name(r.getName())
                .maxGuests(r.getMaxOccupancy())
                .bedType(r.getBedType())
                .sqft(r.getSqft())
                .pricePerNight(r.getPricePerNight())
                .originalPrice(r.getOriginalPrice())
                .tag(r.getTag())
                .features(features)
                .imageSrc(r.getImageSrc())
                .build();
        }).collect(Collectors.toList());

        return PropertyDetailResult.builder()
            .id(property.getId())
            .title(property.getName())
            .location(property.getCity())
            .fullAddress(property.getAddress())
            .propertyType(property.getPropertyType())
            .pricePerNight(roomDTOs.isEmpty() ? BigDecimal.ZERO : roomDTOs.get(0).getPricePerNight())
            .rating(property.getAverageRating() != null ? property.getAverageRating() : 0.0)
            .reviewCount(property.getReviewCount() != null ? property.getReviewCount() : 0)
            .badge(property.getBadge())
            .imageSrc(property.getImageSrc())
            .galleryImages(galleryImages)
            .hostName(property.getHostName())
            .hostBio(property.getHostBio())
            .hostYears(property.getHostYears())
            .hostSuperhost(property.getHostSuperhost())
            .description(property.getDescription())
            .amenities(amenitiesList)
            .reviewBreakdown(new ArrayList<>()) // Simplified for now
            .reviews(new ArrayList<>()) // Simplified for now
            .rooms(roomDTOs)
            .lat(property.getLatitude())
            .lng(property.getLongitude())
            .build();
    }
}