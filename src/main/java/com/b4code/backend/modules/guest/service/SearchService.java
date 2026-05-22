package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dto.SearchDTO.*;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.Review;
import com.b4code.backend.modules.guest.dao.PropertyRepository;
import com.b4code.backend.modules.guest.dao.ReviewRepository;
import com.b4code.backend.modules.guest.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final PropertyRepository propertyRepository;
    private final ReviewRepository reviewRepository;

    // Icon mapping for property types
    private static final Map<String, String> PROPERTY_TYPE_ICONS = Map.of(
        "Villa", "Home",
        "Apartment", "Building2",
        "Guesthouse", "BedDouble",
        "Hotel", "Hotel",
        "Resort", "Palmtree",
        "Cabin", "TreePine"
    );

    public SearchService(
            @Qualifier("guestPropertyRepository") PropertyRepository propertyRepository,
            ReviewRepository reviewRepository) {
        this.propertyRepository = propertyRepository;
        this.reviewRepository = reviewRepository;
    }

    // ─── Paginated Search ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PaginatedResponse<PropertySearchResult> search(
            String destination,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            List<String> propertyTypes,
            List<String> amenities,
            String sortBy,
            int page,
            int size) {

        log.info("Search request: destination={}, guests={}, price=[{}-{}], rating>={}, types={}, sort={}, page={}, size={}",
                destination, guests, minPrice, maxPrice, minRating, propertyTypes, sortBy, page, size);

        int guestsVal = guests != null ? guests : 1;

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
        List<String> types = (propertyTypes != null && !propertyTypes.isEmpty()) ? propertyTypes : java.util.Arrays.asList("Villa", "Hotel", "Guesthouse", "Apartment");

        Page<Property> propertyPage = propertyRepository.searchAvailableProperties(
                safeDestination, checkIn, checkOut, guestsVal,
                safeMinPrice, safeMaxPrice, safeMinRating, types, pageable);

        // Filter by amenities in-memory (amenities stored as comma-separated string)
        List<PropertySearchResult> allResults = propertyPage.getContent().stream()
                .filter(p -> matchesAmenities(p, amenities))
                .map(p -> mapToPropertySearchResult(p, guestsVal))
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
    public PropertyDetailResult getPropertyDetail(Long propertyId) {
        log.info("Fetching property detail for id={}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        if (!property.getPublished()) {
            throw new ResourceNotFoundException("Property not found: " + propertyId);
        }

        return mapToPropertyDetailResult(property);
    }

    // ─── Dynamic Filter Options ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public FilterOptionsResponse getFilterOptions() {
        log.info("Fetching dynamic filter options");

        // Property Types with counts
        List<Object[]> typeCounts = propertyRepository.countByPropertyType();
        List<PropertyTypeOption> typeOptions = typeCounts.stream()
                .map(row -> PropertyTypeOption.builder()
                        .value((String) row[0])
                        .label((String) row[0])
                        .icon(PROPERTY_TYPE_ICONS.getOrDefault((String) row[0], "Home"))
                        .count((Long) row[1])
                        .build())
                .sorted(Comparator.comparing(PropertyTypeOption::getLabel))
                .collect(Collectors.toList());

        // Amenities - collect all distinct amenities from all published properties
        List<Property> allPublished = propertyRepository.findAll().stream()
                .filter(Property::getPublished)
                .collect(Collectors.toList());

        Set<String> amenitySet = new TreeSet<>();
        for (Property p : allPublished) {
            if (p.getAmenities() != null && !p.getAmenities().isEmpty()) {
                for (String am : p.getAmenities().split(",")) {
                    String[] parts = am.trim().split(":");
                    String label = parts.length == 2 ? parts[1].trim() : parts[0].trim();
                    if (!label.isEmpty()) {
                        amenitySet.add(label);
                    }
                }
            }
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
                .propertyTypes(typeOptions)
                .amenities(new ArrayList<>(amenitySet))
                .ratingOptions(ratingOptions)
                .priceRange(priceRange)
                .sortOptions(sortOptions)
                .locations(locations)
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
        if (property.getAmenities() == null || property.getAmenities().isEmpty()) return false;

        String amenitiesLower = property.getAmenities().toLowerCase();
        return amenities.stream().allMatch(am -> amenitiesLower.contains(am.toLowerCase()));
    }

    private PropertySearchResult mapToPropertySearchResult(Property property, int guests) {
        List<Room> availableRooms = property.getRooms() != null
                ? property.getRooms().stream()
                    .filter(Room::getAvailable)
                    .filter(r -> r.getMaxOccupancy() >= guests)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        BigDecimal lowestPrice = availableRooms.stream()
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int maxGuests = availableRooms.stream()
                .mapToInt(Room::getMaxOccupancy)
                .max()
                .orElse(property.getBaseGuests() != null ? property.getBaseGuests() : 2);

        // Extract amenity labels for search card
        List<String> amenityLabels = new ArrayList<>();
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            for (String am : property.getAmenities().split(",")) {
                String[] parts = am.trim().split(":");
                amenityLabels.add(parts.length == 2 ? parts[1].trim() : parts[0].trim());
            }
        }

        return PropertySearchResult.builder()
                .id(property.getId())
                .title(property.getName())
                .location(property.getCity())
                .propertyType(property.getPropertyType())
                .pricePerNight(lowestPrice)
                .maxGuests(maxGuests)
                .baseGuests(property.getBaseGuests() != null ? property.getBaseGuests() : 2)
                .extraGuestFee(property.getExtraGuestFee())
                .rating(property.getAverageRating() != null ? property.getAverageRating() : 0.0)
                .reviewCount(property.getReviewCount() != null ? property.getReviewCount() : 0)
                .badge(property.getBadge())
                .imageSrc(property.getImageSrc())
                .amenities(amenityLabels)
                .lat(property.getLatitude())
                .lng(property.getLongitude())
                .build();
    }

    private PropertyDetailResult mapToPropertyDetailResult(Property property) {
        // Gallery images
        List<String> galleryImages = property.getGalleryImages() != null && !property.getGalleryImages().isEmpty()
                ? Arrays.asList(property.getGalleryImages().split(","))
                : new ArrayList<>();

        // Amenities
        List<AmenityDTO> amenitiesList = new ArrayList<>();
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            for (String am : property.getAmenities().split(",")) {
                String[] parts = am.trim().split(":");
                if (parts.length == 2) {
                    amenitiesList.add(new AmenityDTO(parts[0].trim(), parts[1].trim()));
                } else {
                    amenitiesList.add(new AmenityDTO("Check", parts[0].trim()));
                }
            }
        }

        // Reviews from database
        List<Review> dbReviews = reviewRepository.findByPropertyIdOrderByCreatedAtDesc(property.getId());
        List<ReviewDTO> reviewDTOs = dbReviews.stream().map(r -> ReviewDTO.builder()
                .id(r.getId().toString())
                .author(r.getGuestName() != null ? r.getGuestName() : "Anonymous")
                .avatarInitials(getInitials(r.getGuestName()))
                .avatarColor(getAvatarColor(r.getId()))
                .date(r.getCreatedAt() != null
                        ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                        : "Recent")
                .text(r.getComment() != null ? r.getComment() : "")
                .rating(r.getOverallRating())
                .ownerReply(r.getOwnerResponse())
                .build()
        ).collect(Collectors.toList());

        // Review breakdown
        List<ReviewBreakdownDTO> breakdown = new ArrayList<>();
        if (!dbReviews.isEmpty()) {
            breakdown.add(new ReviewBreakdownDTO("Cleanliness",
                    dbReviews.stream().filter(r -> r.getCleanlinessRating() != null).mapToInt(Review::getCleanlinessRating).average().orElse(0.0)));
            breakdown.add(new ReviewBreakdownDTO("Accuracy",
                    dbReviews.stream().filter(r -> r.getAccuracyRating() != null).mapToInt(Review::getAccuracyRating).average().orElse(0.0)));
            breakdown.add(new ReviewBreakdownDTO("Communication",
                    dbReviews.stream().filter(r -> r.getCommunicationRating() != null).mapToInt(Review::getCommunicationRating).average().orElse(0.0)));
            breakdown.add(new ReviewBreakdownDTO("Location",
                    dbReviews.stream().filter(r -> r.getLocationRating() != null).mapToInt(Review::getLocationRating).average().orElse(0.0)));
            breakdown.add(new ReviewBreakdownDTO("Value",
                    dbReviews.stream().filter(r -> r.getValueRating() != null).mapToInt(Review::getValueRating).average().orElse(0.0)));
        }

        // Rooms
        List<RoomDTO> roomDTOs = property.getRooms() != null
                ? property.getRooms().stream().map(r -> {
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
                }).collect(Collectors.toList())
                : new ArrayList<>();

        BigDecimal price = roomDTOs.isEmpty() ? BigDecimal.ZERO : roomDTOs.get(0).getPricePerNight();

        return PropertyDetailResult.builder()
                .id(property.getId())
                .title(property.getName())
                .location(property.getCity())
                .fullAddress(property.getAddress())
                .propertyType(property.getPropertyType())
                .pricePerNight(price)
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
