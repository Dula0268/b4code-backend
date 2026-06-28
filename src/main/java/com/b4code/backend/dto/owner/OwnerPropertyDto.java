package com.b4code.backend.dto.owner;

import com.b4code.backend.models.Amenity;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.Room;
import com.b4code.backend.models.enums.PropertyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerPropertyDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private String image;
    private String rate;
    private Double rating;
    private Integer reviews;
    private String status;
    private Boolean statusOn;
    private String checkIn;
    private String checkOut;
    private String houseRules;
    private String cancellationPolicy;
    private String contactPhone;
    private String contactEmail;
    private String propertyType;
    private List<String> amenities;
    private List<String> photoUrls;
    private Integer roomCount;

    public static OwnerPropertyDto fromEntity(Property p) {
        BigDecimal minPrice = p.getRooms().stream()
                .map(Room::getPricePerNight)
                .min(Comparator.naturalOrder())
                .orElse(null);

        String formattedRate = minPrice != null
                ? "$" + minPrice.stripTrailingZeros().toPlainString()
                : "—";

        String statusStr = toStatusString(p.getStatus());
        boolean statusOn = p.getStatus() == PropertyStatus.ACTIVE;

        List<String> amenityNames = p.getAmenities() == null ? List.of()
                : p.getAmenities().stream().map(Amenity::getName).toList();

        List<String> photoUrls = new java.util.ArrayList<>();
        if (p.getImages() != null) {
            p.getImages().stream()
                    .filter(img -> img.getUrl() != null)
                    .map(img -> img.getUrl())
                    .forEach(photoUrls::add);
        }
        if (photoUrls.isEmpty() && p.getImageUrl() != null) {
            photoUrls.add(p.getImageUrl());
        }

        String image = photoUrls.isEmpty() ? null : photoUrls.get(0);

        return OwnerPropertyDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .address(p.getAddressLine1() != null ? p.getAddressLine1() : p.getAddress())
                .city(p.getCity())
                .country(p.getCountry())
                .image(image)
                .rate(formattedRate)
                .rating(p.getAverageRating())
                .reviews(p.getReviewCount())
                .status(statusStr)
                .statusOn(statusOn)
                .checkIn(p.getCheckInTime())
                .checkOut(p.getCheckOutTime())
                .houseRules(p.getHouseRules())
                .cancellationPolicy(p.getCancellationPolicy())
                .contactPhone(p.getContactPhone())
                .contactEmail(p.getContactEmail())
                .propertyType(p.getPropertyType())
                .amenities(amenityNames)
                .photoUrls(photoUrls)
                .roomCount(p.getRooms() != null ? p.getRooms().size() : 0)
                .build();
    }

    private static String toStatusString(PropertyStatus s) {
        if (s == null) return "pending";
        return switch (s) {
            case ACTIVE       -> "active";
            case INACTIVE     -> "inactive";
            case MAINTENANCE  -> "maintenance";
            case APPROVED     -> "active";
            case PENDING      -> "pending";
            case UNDER_REVIEW -> "under_review";
            case REJECTED     -> "rejected";
        };
    }
}
