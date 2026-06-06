package com.b4code.backend.service.impl;

import com.b4code.backend.dto.SearchDTO.*;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.Review;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.ReviewRepository;
import com.b4code.backend.exceptions.ResourceNotFoundException;
import com.b4code.backend.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.b4code.backend.dao.BookingRepository;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final PropertyRepository propertyRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public SearchServiceImpl(PropertyRepository propertyRepository, ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.propertyRepository = propertyRepository;
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    // ─── Paginated Search ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PropertySearchResult> search(
            String destination,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests,
            Integer rooms,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            
            List<String> amenities,
            String sortBy,
            int page,
            int size) {

        log.info("Search request: destination={}, guests={}, rooms={}, price=[{}-{}], rating>={}, types={}, sort={}, page={}, size={}",
                destination, guests, rooms, minPrice, maxPrice, minRating, sortBy, page, size);

        int guestsVal = guests != null ? guests : 1;
        int roomsVal = rooms != null ? rooms : 1;

        // Build sort
        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Provide safe defaults to avoid PostgreSQL bytea null casting issues
        String safeDestination = destination != null ? destination : "";
        BigDecimal safeMinPrice = minPrice != null ? minPrice : BigDecimal.ZERO;
        BigDecimal safeMaxPrice = maxPrice != null ? maxPrice : new BigDecimal("10000000");
        Double safeMinRating = minRating != null ? minRating : 0.0;
        

        Page<Property> propertyPage = propertyRepository.searchAvailableProperties(
                safeDestination, checkIn, checkOut, roomsVal,
                safeMinPrice, safeMaxPrice, pageable);

        // Filter by amenities in-memory (amenities stored as comma-separated string)
        List<PropertySearchResult> results = propertyPage.getContent().stream()
                .filter(p -> matchesAmenities(p, amenities))
                .filter(p -> {
                    Double rating = p.getAverageRating();
                    return rating != null ? rating >= safeMinRating : safeMinRating <= 0.0;
                })
                .map(p -> mapToPropertySearchResult(p, guestsVal, checkIn, checkOut))
                .filter(p -> p.getMatchingRoomsCount() >= roomsVal) // Double check room count
                .collect(Collectors.toList());

        log.info("Search returned {} results (page {} of {})",
                results.size(), page + 1, propertyPage.getTotalPages());

        return PaginatedResponse.<PropertySearchResult>builder()
                .content(results)
                .page(page)
                .size(size)
                .totalElements(propertyPage.getTotalElements())
                .totalPages(propertyPage.getTotalPages())
                .first(page == 0)
                .last(page >= propertyPage.getTotalPages() - 1)
                .build();
    }

    // ─── Property Detail ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailResult getPropertyDetail(Long propertyId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Fetching property detail for id={}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        return mapToPropertyDetailResult(property, checkIn, checkOut);
    }

    // ─── Dynamic Filter Options ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public FilterOptionsResponse getFilterOptions() {
        log.info("Fetching dynamic filter options");

        // Property Types with counts
        

        // Amenities - collect all distinct amenities from all published properties
        List<Property> allPublished = propertyRepository.findAll();

        Set<String> distinctPropertyNames = new TreeSet<>();

        for (Property p : allPublished) {
            if (p.getName() != null && !p.getName().isBlank()) {
                distinctPropertyNames.add(p.getName());
            }
        }

        List<LocationSuggestionDTO> locationSuggestions = new ArrayList<>();
        for (String p : distinctPropertyNames) {
            locationSuggestions.add(new LocationSuggestionDTO(p, "property"));
        }

        // Price range
        BigDecimal minPrice = propertyRepository.findMinPrice();
        BigDecimal maxPrice = propertyRepository.findMaxPrice();
        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (maxPrice == null) maxPrice = new BigDecimal("500000");

        PriceRangeOption priceRange = PriceRangeOption.builder()
                .min(minPrice)
                .max(maxPrice)
                .currency("LKR")
                .build();

        // Rating options
        List<RatingOption> ratingOptions = List.of(
                RatingOption.builder().label("5.0 only").value("5.0").build(),
                RatingOption.builder().label("4.5 & up").value("4.5").build(),
                RatingOption.builder().label("4.0 & up").value("4.0").build(),
                RatingOption.builder().label("3.5 & up").value("3.5").build()
        );

        // Locations
        List<String> locations = propertyRepository.findDistinctCities();

        return FilterOptionsResponse.builder()
                .propertyTypes(new ArrayList<>())
                .amenities(new ArrayList<>())
                .ratingOptions(ratingOptions)
                .priceRange(priceRange)
                .sortOptions(new ArrayList<>())
                .locations(locations)
                .locationSuggestions(locationSuggestions)
                .build();
    }

    // ─── Private Helpers ─────────────────────────────────────────────────

    private Sort buildSort(String sortBy) {
        if (sortBy == null) return Sort.unsorted();
        return switch (sortBy) {
            // Price sorts are handled in-memory (cross-entity JOIN)
            case "price_asc", "price_desc" -> Sort.unsorted();
            case "rating"     -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "reviews"    -> Sort.by(Sort.Direction.DESC, "reviewCount");
            default           -> Sort.unsorted(); // "recommended" = natural order
        };
    }

    private boolean matchesAmenities(Property property, List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) return true;

        String amenitiesLower = property.getAmenities() != null 
            ? property.getAmenities().stream().map(a -> a.getName().toLowerCase()).collect(Collectors.joining(","))
            : "";

        return amenities.stream().allMatch(am -> {
            String amLower = am.toLowerCase();
            System.out.println("Checking amenity: " + amLower + " for property " + property.getName());
            
            // Handle special advanced filter toggles
            if (amLower.equals("free cancellation")) {
                return property.getFreeCancellation() != null && property.getFreeCancellation();
            }
            if (amLower.equals("breakfast included")) {
                return property.getBreakfastIncluded() != null && property.getBreakfastIncluded();
            }
            if (amLower.equals("pet-friendly")) {
                return property.getPetFriendly() != null && property.getPetFriendly();
            }
            if (amLower.equals("accessibility")) {
                return property.getAccessibility() != null && property.getAccessibility();
            }

            // Normal amenity check
            return amenitiesLower.contains(amLower);
        });
    }

    private PropertySearchResult mapToPropertySearchResult(Property property, int guests, LocalDate checkIn, LocalDate checkOut) {
        List<Room> availableRooms = property.getRooms() != null
                ? property.getRooms().stream()
                    .filter(r -> {
                        if (checkIn == null || checkOut == null) return true;
                        return !bookingRepository.existsOverlappingBooking(r.getId(), checkIn, checkOut);
                    })
                    .collect(Collectors.toList())
                : Collections.emptyList();

        int matchingRoomsCount = availableRooms.size();

        BigDecimal lowestPrice = availableRooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int maxGuests = 2; // Defaulting since maxOccupancy is removed

        Double avgRating = property.getAverageRating();
        Integer reviewCount = property.getReviewCount();

        String primaryImage = "/images/placeholder-property.jpg";
        if (property.getImages() != null && !property.getImages().isEmpty()) {
            primaryImage = property.getImages().stream()
                .filter(img -> com.b4code.backend.models.ImageType.PROPERTY.equals(img.getType()))
                .map(com.b4code.backend.models.Image::getUrl)
                .findFirst()
                .orElse(property.getImages().get(0).getUrl());
        }

        return PropertySearchResult.builder()
                .id(property.getId())
                .title(property.getName())
                .location(property.getCity())
                .district(property.getCity())
                
                .pricePerNight(lowestPrice)
                .maxGuests(maxGuests)
                .baseGuests(2)
                .extraGuestFee(BigDecimal.ZERO)
                .rating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .badge("")
                .imageSrc(primaryImage)
                .matchingRoomsCount(matchingRoomsCount)
                .build();
    }

    private PropertyDetailResult mapToPropertyDetailResult(Property property, LocalDate checkIn, LocalDate checkOut) {
        // Gallery images
        List<String> galleryImages = property.getImages() != null
                ? property.getImages().stream().map(com.b4code.backend.models.Image::getUrl).collect(java.util.stream.Collectors.toList())
                : new java.util.ArrayList<>();
            
        String primaryImage = galleryImages.isEmpty() ? "/images/placeholder-property.jpg" : galleryImages.get(0);

        // Amenities
        List<AmenityDTO> amenitiesList = new ArrayList<>();
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            for (com.b4code.backend.models.Amenity am : property.getAmenities()) {
                amenitiesList.add(new AmenityDTO("Check", am.getName()));
            }
        }

        // Reviews from database
        List<Review> dbReviews = reviewRepository.findByPropertyIdOrderByIdDesc(property.getId());
        List<ReviewDTO> reviewDTOs = dbReviews.stream().map(r -> ReviewDTO.builder()
                .id(r.getId().toString())
                .author(r.getGuest() != null ? r.getGuest().getFullName() : "Anonymous")
                .avatarInitials(getInitials(r.getGuest() != null ? r.getGuest().getFullName() : "Anonymous"))
                .avatarColor(getAvatarColor(r.getId()))
                .date("Recent")
                .text(r.getComment() != null ? r.getComment() : "")
                .rating(r.getOverallRating())
                .build()
        ).collect(Collectors.toList());

        // Review breakdown
        List<ReviewBreakdownDTO> breakdown = new ArrayList<>();

        // Rooms
        List<RoomDTO> roomDTOs = new ArrayList<>();
        if (property.getRooms() != null) {
            List<Room> availableRooms = property.getRooms().stream()
                .filter(r -> {
                    if (checkIn == null || checkOut == null) return true;
                    return !bookingRepository.existsOverlappingBooking(r.getId(), checkIn, checkOut);
                })
                .collect(Collectors.toList());

            Map<String, List<Room>> groupedRooms = availableRooms.stream()
                .collect(Collectors.groupingBy(r -> 
                    (r.getRoomType() != null ? r.getRoomType().name() : "UNKNOWN") + "|" +
                    (r.getBedType() != null ? r.getBedType().name() : "UNKNOWN") + "|" +
                    (r.getMaxOccupancy() != null ? r.getMaxOccupancy() : 2) + "|" +
                    (r.getPricePerNight() != null ? r.getPricePerNight().toString() : "0")
                ));

            for (List<Room> group : groupedRooms.values()) {
                Room first = group.get(0);
                roomDTOs.add(RoomDTO.builder()
                        .id(first.getId().toString())
                        .name(first.getRoomType() != null ? first.getRoomType().name() : "")
                        .maxGuests(first.getMaxOccupancy() != null ? first.getMaxOccupancy() : 2)
                        .bedType(first.getBedType() != null ? first.getBedType().name() : "")
                        .numberOfRooms(group.size())
                        .sqft(0)
                        .pricePerNight(first.getPricePerNight())
                        .originalPrice(first.getPricePerNight())
                        .tag("")
                        .features(new ArrayList<>())
                        .imageSrc(first.getImage() != null ? first.getImage().getUrl() : null)
                        .build());
            }
        }

        BigDecimal price = roomDTOs.isEmpty() ? BigDecimal.ZERO : roomDTOs.get(0).getPricePerNight();
        
        Double avgRating = property.getAverageRating();
        Integer reviewCount = property.getReviewCount();

        return PropertyDetailResult.builder()
                .id(property.getId())
                .title(property.getName())
                .location(property.getCity())
                .fullAddress((property.getAddressLine1() != null ? property.getAddressLine1() : "") + ", " + (property.getCity() != null ? property.getCity() : "") + ", " + (property.getCountry() != null ? property.getCountry() : ""))
                
                .pricePerNight(price)
                .rating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .badge("")
                .imageSrc(primaryImage)
                .galleryImages(galleryImages)
                .hostName("Prime Stay")
                .hostBio("")
                .hostYears(1)
                .hostSuperhost(false)
                .description(property.getDescription())
                .amenities(amenitiesList)
                .reviewBreakdown(breakdown)
                .reviews(reviewDTOs)
                .rooms(roomDTOs)
                .lat(property.getLatitude())
                .lng(property.getLongitude())
                .build();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private static final String[] AVATAR_COLORS = {
        "#953002", "#2E7D32", "#1565C0", "#6A1B9A", "#E65100",
        "#00695C", "#AD1457", "#4527A0", "#C62828", "#1B5E20"
    };

    private String getAvatarColor(Long id) {
        return AVATAR_COLORS[(int) (id % AVATAR_COLORS.length)];
    }
}
