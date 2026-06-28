package com.b4code.backend.service;

import com.b4code.backend.dto.SearchDTO.*;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.Review;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.ReviewRepository;
import com.b4code.backend.exceptions.ResourceNotFoundException;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.b4code.backend.dao.BookingRepository;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final PropertyRepository propertyRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    // Icon mapping for property types
    private static final Map<String, String> PROPERTY_TYPE_ICONS = Map.of(
        "Villa", "Home",
        "Apartment", "Building2",
        "Guesthouse", "BedDouble",
        "Hotel", "Hotel",
        "Resort", "Palmtree",
        "Cabin", "TreePine"
    );

    public SearchService(PropertyRepository propertyRepository, ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.propertyRepository = propertyRepository;
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    // ─── Paginated Search ────────────────────────────────────────────────

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

        // Build sort — price sorts are done in-memory since they cross a JOIN
        Sort sort = buildSort(sortBy);
        boolean isPriceSort = "price_asc".equals(sortBy) || "price_desc".equals(sortBy);
        Pageable pageable = isPriceSort
                ? PageRequest.of(0, Integer.MAX_VALUE) // Fetch all for in-memory sort
                : PageRequest.of(page, size, sort);

        // Provide safe defaults to avoid PostgreSQL bytea null casting issues
        String safeDestination = destination != null ? destination : "";
        BigDecimal safeMinPrice = minPrice != null ? minPrice : BigDecimal.ZERO;
        BigDecimal safeMaxPrice = maxPrice != null ? maxPrice : new BigDecimal("10000000");
        Double safeMinRating = minRating != null ? minRating : 0.0;
        
        boolean hasDates = (checkIn != null && checkOut != null);
        LocalDate safeCheckIn = checkIn != null ? checkIn : LocalDate.of(1970, 1, 1);
        LocalDate safeCheckOut = checkOut != null ? checkOut : LocalDate.of(1970, 1, 1);

        Page<Property> propertyPage = propertyRepository.searchAvailableProperties(
                safeDestination, hasDates, safeCheckIn, safeCheckOut, guestsVal, roomsVal,
                safeMinPrice, safeMaxPrice, pageable);

        // Filter by amenities in-memory (amenities stored as comma-separated string)
        List<PropertySearchResult> allResults = propertyPage.getContent().stream()
                .filter(p -> matchesAmenities(p, amenities))
                .map(p -> mapToPropertySearchResult(p, guestsVal, checkIn, checkOut))
                .filter(p -> p.getMatchingRoomsCount() >= roomsVal) // Double check room count
                .filter(p -> p.getRating() >= safeMinRating) // Filter by minimum rating
                .collect(Collectors.toList());

        // Apply in-memory price sorting if needed
        if (isPriceSort) {
            Comparator<PropertySearchResult> priceComp = Comparator.comparing(
                    PropertySearchResult::getPricePerNight, Comparator.nullsLast(BigDecimal::compareTo));
            if ("price_desc".equals(sortBy)) priceComp = priceComp.reversed();
            allResults.sort(priceComp);
        }

        // Apply manual pagination if we fetched all
        long totalElements;
        int totalPages;
        List<PropertySearchResult> results;
        if (isPriceSort) {
            totalElements = allResults.size();
            totalPages = (int) Math.ceil((double) totalElements / size);
            int from = Math.min(page * size, allResults.size());
            int to = Math.min(from + size, allResults.size());
            results = allResults.subList(from, to);
        } else {
            totalElements = propertyPage.getTotalElements();
            totalPages = propertyPage.getTotalPages();
            results = allResults;
        }

        log.info("Search returned {} results (page {} of {})",
                results.size(), page + 1, totalPages);

        return PaginatedResponse.<PropertySearchResult>builder()
                .content(results)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    // ─── Property Detail ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PropertyDetailResult getPropertyDetail(Long propertyId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Fetching property detail for id={}, checkIn={}, checkOut={}", propertyId, checkIn, checkOut);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        return mapToPropertyDetailResult(property, checkIn, checkOut);
    }

    // ─── Dynamic Filter Options ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public FilterOptionsResponse getFilterOptions() {
        log.info("Fetching dynamic filter options");

        // Property Types with counts
        

        // Amenities - collect all distinct amenities from all published properties
        List<Property> allPublished = propertyRepository.findAll();

        Set<String> amenitySet = new TreeSet<>();
        Set<String> distinctDistricts = new TreeSet<>();
        Set<String> distinctPropertyNames = new TreeSet<>();

        for (Property p : allPublished) {
            if (p.getAmenities() != null && !p.getAmenities().isEmpty()) {
                for (com.b4code.backend.models.Amenity am : p.getAmenities()) {
                    if (am.getName() != null && !am.getName().isEmpty()) {
                        amenitySet.add(am.getName());
                    }
                }
            }
            if (p.getCity() != null && !p.getCity().isBlank()) {
                distinctDistricts.add(p.getCity());
            }
            if (p.getName() != null && !p.getName().isBlank()) {
                distinctPropertyNames.add(p.getName());
            }
        }

        List<LocationSuggestionDTO> locationSuggestions = new ArrayList<>();
        for (String d : distinctDistricts) {
            locationSuggestions.add(new LocationSuggestionDTO(d, "district"));
        }
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

        // Sort options
        List<SortOption> sortOptions = List.of(
                SortOption.builder().value("recommended").label("Recommended").build(),
                SortOption.builder().value("price_asc").label("Price: Low to High").build(),
                SortOption.builder().value("price_desc").label("Price: High to Low").build(),
                SortOption.builder().value("rating").label("Highest Rated").build(),
                SortOption.builder().value("reviews").label("Most Reviewed").build()
        );

        // Locations
        List<String> locations = propertyRepository.findDistinctCities();

        return FilterOptionsResponse.builder()
                .propertyTypes(new ArrayList<>())
                .amenities(new ArrayList<>(amenitySet))
                .ratingOptions(ratingOptions)
                .priceRange(priceRange)
                .sortOptions(sortOptions)
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
            if (amLower.equals("breakfast included") || amLower.equals("breakfast")) {
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
                        int booked = bookingRepository.getBookedQuantityForDates(r.getId(), checkIn, checkOut);
                        return (r.getInventory() - booked) > 0;
                    })
                    .collect(Collectors.toList())
                : Collections.emptyList();

        int matchingRoomsCount = availableRooms.size();

        BigDecimal lowestPrice = availableRooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal highestPrice = availableRooms.stream()
                .map(Room::getPricePerNight)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int maxGuests = 2; // Defaulting since maxOccupancy is removed

        // Extract amenity labels for search card (only advanced filters as requested)
        List<String> amenityLabels = new ArrayList<>();
        
        if (Boolean.TRUE.equals(property.getFreeCancellation())) amenityLabels.add("Free Cancellation");
        if (Boolean.TRUE.equals(property.getBreakfastIncluded())) amenityLabels.add("Breakfast Included");
        if (Boolean.TRUE.equals(property.getPetFriendly())) amenityLabels.add("Pet-Friendly");
        if (Boolean.TRUE.equals(property.getAccessibility())) amenityLabels.add("Accessibility");
        
        Double avgRating = reviewRepository.calculateAverageRating(property.getId());
        Long reviewCount = reviewRepository.countByPropertyId(property.getId());

        String primaryImage = "/images/placeholder-property.jpg";
        if (property.getImages() != null && !property.getImages().isEmpty()) {
            primaryImage = property.getImages().stream()
                .filter(img -> com.b4code.backend.models.ImageType.PROPERTY.equals(img.getType()) || com.b4code.backend.models.ImageType.GALLERY.equals(img.getType()))
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
                .highestPricePerNight(highestPrice)
                .maxGuests(maxGuests)
                .baseGuests(2)
                .extraGuestFee(BigDecimal.ZERO)
                .rating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount.intValue() : 0)
                .badge("")
                .imageSrc(primaryImage)
                .amenities(amenityLabels)
                .lat(property.getLatitude())
                .lng(property.getLongitude())
                .matchingRoomsCount(matchingRoomsCount)
                .build();
    }

    private PropertyDetailResult mapToPropertyDetailResult(Property property, LocalDate checkIn, LocalDate checkOut) {
        // Gallery images
        List<String> galleryImages = property.getImages() != null
                ? property.getImages().stream()
                    .filter(img -> com.b4code.backend.models.ImageType.PROPERTY.equals(img.getType()) || com.b4code.backend.models.ImageType.GALLERY.equals(img.getType()))
                    .map(com.b4code.backend.models.Image::getUrl)
                    .collect(java.util.stream.Collectors.toList())
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
        List<Review> dbReviews = reviewRepository.findByPropertyIdOrderByCreatedAtDesc(property.getId());
        List<ReviewDTO> reviewDTOs = dbReviews.stream().map(r -> ReviewDTO.builder()
                .id(r.getId().toString())
                .author(r.getGuest() != null ? r.getGuest().getFullName() : "Anonymous")
                .avatarInitials(getInitials(r.getGuest() != null ? r.getGuest().getFullName() : "Anonymous"))
                .avatarColor(getAvatarColor(r.getId()))
                .date(r.getCreatedAt() != null
                        ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                        : "Recent")
                .text(r.getComment() != null ? r.getComment() : "")
                .rating(r.getOverallRating())
                .cleanlinessRating(r.getCleanlinessRating())
                .comfortRating(r.getComfortRating())
                .serviceRating(r.getServiceRating())
                .diningRating(r.getDiningRating())
                .locationRating(r.getLocationRating())
                .valueRating(r.getValueRating())
                .photoUrls(r.getPhotoUrls() != null ? Arrays.asList(r.getPhotoUrls().split(",")) : new ArrayList<>())
                .build()
        ).collect(Collectors.toList());

        // Review breakdown
        List<ReviewBreakdownDTO> breakdown = new ArrayList<>();
        if (property.getAvgCleanliness() != null) breakdown.add(new ReviewBreakdownDTO("Cleanliness", property.getAvgCleanliness()));
        if (property.getAvgComfort() != null) breakdown.add(new ReviewBreakdownDTO("Comfort", property.getAvgComfort()));
        if (property.getAvgService() != null) breakdown.add(new ReviewBreakdownDTO("Service", property.getAvgService()));
        if (property.getAvgDining() != null) breakdown.add(new ReviewBreakdownDTO("Dining", property.getAvgDining()));
        if (property.getAvgLocation() != null) breakdown.add(new ReviewBreakdownDTO("Location", property.getAvgLocation()));
        if (property.getAvgValue() != null) breakdown.add(new ReviewBreakdownDTO("Value", property.getAvgValue()));

        // Rooms
        List<RoomDTO> roomDTOs = property.getRooms() != null
                ? property.getRooms().stream()
                    .filter(r -> {
                        if (checkIn == null || checkOut == null) return true;
                        int booked = bookingRepository.getBookedQuantityForDates(r.getId(), checkIn, checkOut);
                        return (r.getInventory() - booked) > 0;
                    })
                    .map(r -> {
                        int availableCount = r.getInventory() != null ? r.getInventory() : 3;
                        if (checkIn != null && checkOut != null) {
                            int booked = bookingRepository.getBookedQuantityForDates(r.getId(), checkIn, checkOut);
                            availableCount -= booked;
                        }
                    return RoomDTO.builder()
                            .id(r.getId().toString())
                            .name(r.getRoomType() != null ? r.getRoomType().name() : "")
                            .maxGuests(r.getMaxOccupancy() != null ? r.getMaxOccupancy() : 2)
                            .bedType(r.getBedType() != null ? r.getBedType().name() : "")
                            .sqft(0)
                            .pricePerNight(r.getPricePerNight())
                            .originalPrice(r.getPricePerNight())
                            .tag("")
                            .features(new ArrayList<>())
                            .imageSrc(r.getImage() != null ? r.getImage().getUrl() : null)
                            .availableCount(availableCount)
                            .build();
                }).collect(Collectors.toList())
                : new ArrayList<>();

        BigDecimal price = roomDTOs.isEmpty() ? BigDecimal.ZERO : roomDTOs.get(0).getPricePerNight();
        
        Double avgRating = reviewRepository.calculateAverageRating(property.getId());
        Long reviewCount = reviewRepository.countByPropertyId(property.getId());

        return PropertyDetailResult.builder()
                .id(property.getId())
                .title(property.getName())
                .location(property.getCity())
                .fullAddress((property.getAddressLine1() != null ? property.getAddressLine1() : "") + ", " + (property.getCity() != null ? property.getCity() : "") + ", " + (property.getCountry() != null ? property.getCountry() : ""))
                
                .pricePerNight(price)
                .rating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount.intValue() : 0)
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
                .checkInTime(property.getCheckInTime())
                .checkOutTime(property.getCheckOutTime())
                .cancellationPolicy(property.getCancellationPolicy())
                .childPolicy(property.getChildPolicy())
                .houseRules(property.getHouseRules())
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
