package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.owner.OwnerPropertyDto;
import com.b4code.backend.dto.owner.OwnerPropertyPageDto;
import com.b4code.backend.dto.owner.OwnerPropertyRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Amenity;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.PropertyStatus;
import com.b4code.backend.service.AdminNotificationService;
import com.b4code.backend.infrastructure.storage.CloudinaryService;
import com.b4code.backend.service.OwnerPropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerPropertyServiceImpl implements OwnerPropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final AdminNotificationService adminNotificationService;

    @Override
    @Transactional(readOnly = true)
    public OwnerPropertyPageDto listProperties(String ownerEmail, int page, int size, String search, String status) {
        User owner = resolveOwner(ownerEmail);
        int zeroPage = Math.max(0, page - 1);

        PropertyStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try { statusEnum = PropertyStatus.valueOf(status.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }

        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        Page<Property> pageResult = propertyRepository.findByOwnerWithFilters(
                owner.getId(), searchTerm, statusEnum, PageRequest.of(zeroPage, size));

        List<OwnerPropertyDto> dtos = pageResult.getContent().stream()
                .map(OwnerPropertyDto::fromEntity)
                .toList();

        return OwnerPropertyPageDto.builder()
                .properties(dtos)
                .currentPage(page)
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerPropertyDto getProperty(String ownerEmail, Long propertyId) {
        return OwnerPropertyDto.fromEntity(resolveOwnedProperty(ownerEmail, propertyId));
    }

    @Override
    @Transactional
    public OwnerPropertyDto createProperty(String ownerEmail, OwnerPropertyRequest request) {
        User owner = resolveOwner(ownerEmail);

        Property property = Property.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .name(request.getPropertyName())
                .description(request.getDescription())
                .addressLine1(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .checkInTime(request.getCheckInTime() != null ? request.getCheckInTime() : "14:00")
                .checkOutTime(request.getCheckOutTime() != null ? request.getCheckOutTime() : "11:00")
                .houseRules(request.getCustomRules())
                .propertyType(request.getPropertyType())
                .imageUrl(request.getCoverPhoto())
                .galleryImages(request.getImages() != null ? String.join(",", request.getImages()) : null)
                .status(PropertyStatus.PENDING)
                .build();

        attachAmenities(property, request.getAmenities());
        
        if (request.getRooms() != null && !request.getRooms().isEmpty()) {
            for (OwnerPropertyRequest.RoomRequest rm : request.getRooms()) {
                com.b4code.backend.models.RoomType roomType = com.b4code.backend.models.RoomType.builder()
                        .property(property)
                        .name(rm.getName())
                        .maxOccupancy(rm.getMaxCapacity() != null ? rm.getMaxCapacity() : 2)
                        .pricePerNight(java.math.BigDecimal.ZERO)
                        .inventory(1)
                        .roomCategory(com.b4code.backend.models.RoomCategory.STANDARD_ROOM)
                        .status(com.b4code.backend.models.enums.RoomStatus.AVAILABLE)
                        .build();
                property.getRoomTypes().add(roomType);
            }
        }

        Property saved = propertyRepository.save(property);
        log.info("Owner {} created property id={}", ownerEmail, saved.getId());

        // Save images to the Image entity table so admin can see them
        if (request.getCoverPhoto() != null && !request.getCoverPhoto().isBlank()) {
            com.b4code.backend.models.Image cover = com.b4code.backend.models.Image.builder()
                    .url(request.getCoverPhoto())
                    .type(com.b4code.backend.models.ImageType.PROPERTY)
                    .property(saved)
                    .build();
            saved.getImages().add(cover);
        }
        if (request.getImages() != null) {
            for (String imgUrl : request.getImages()) {
                if (imgUrl != null && !imgUrl.isBlank() && !imgUrl.equals(request.getCoverPhoto())) {
                    com.b4code.backend.models.Image img = com.b4code.backend.models.Image.builder()
                            .url(imgUrl)
                            .type(com.b4code.backend.models.ImageType.GALLERY)
                            .property(saved)
                            .build();
                    saved.getImages().add(img);
                }
            }
        }
        propertyRepository.save(saved);

        // Notify Admin
        adminNotificationService.createNotification(
            "New Property Registration",
            "A new property '" + saved.getName() + "' requires verification.",
            com.b4code.backend.models.enums.AdminNotificationType.NEW_PROPERTY,
            saved.getId().toString()
        );

        return OwnerPropertyDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerPropertyDto updateProperty(String ownerEmail, Long propertyId, OwnerPropertyRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, propertyId);

        // ── Status Transition ─────────────────────────────────────────
        // If the property is ACTIVE or APPROVED, ANY modification by the owner
        // will unpublish it and revert it to PENDING so the admin can re-verify.
        boolean isPublished = property.getStatus() == PropertyStatus.ACTIVE
                           || property.getStatus() == PropertyStatus.APPROVED;

        if (request.getPropertyName() != null)  property.setName(request.getPropertyName());
        if (request.getDescription() != null)   property.setDescription(request.getDescription());
        if (request.getAddress() != null)       property.setAddressLine1(request.getAddress());
        if (request.getCity() != null)          property.setCity(request.getCity());
        if (request.getCountry() != null)       property.setCountry(request.getCountry());
        if (request.getLatitude() != null)      property.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)     property.setLongitude(request.getLongitude());
        if (request.getCheckInTime() != null)   property.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null)  property.setCheckOutTime(request.getCheckOutTime());
        if (request.getCustomRules() != null)   property.setHouseRules(request.getCustomRules());
        if (request.getPropertyType() != null)  property.setPropertyType(request.getPropertyType());
        if (request.getCoverPhoto() != null)    property.setImageUrl(request.getCoverPhoto());
        if (request.getImages() != null)        property.setGalleryImages(String.join(",", request.getImages()));

        if (request.getAmenities() != null) {
            property.getAmenities().clear();
            attachAmenities(property, request.getAmenities());
        }

        if (isPublished) {
            property.setStatus(PropertyStatus.PENDING);
            property.setSubmittedAt(java.time.LocalDateTime.now());
            log.info("Owner {} updated an approved/active property id={} — reverted to PENDING", ownerEmail, propertyId);

            adminNotificationService.createNotification(
                "Property Re-review Required",
                "Property '" + property.getName() + "' has been updated by the owner and requires re-verification.",
                com.b4code.backend.models.enums.AdminNotificationType.NEW_PROPERTY,
                property.getId().toString()
            );
        }

        Property saved = propertyRepository.save(property);
        log.info("Owner {} updated property id={}", ownerEmail, propertyId);
        return OwnerPropertyDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteProperty(String ownerEmail, Long propertyId) {
        Property property = resolveOwnedProperty(ownerEmail, propertyId);
        propertyRepository.delete(property);
        log.info("Owner {} deleted property id={}", ownerEmail, propertyId);
    }

    @Override
    @Transactional
    public OwnerPropertyDto toggleStatus(String ownerEmail, Long propertyId) {
        Property property = resolveOwnedProperty(ownerEmail, propertyId);

        if (property.getStatus() == PropertyStatus.PENDING ||
            property.getStatus() == PropertyStatus.UNDER_REVIEW ||
            property.getStatus() == PropertyStatus.REJECTED) {
            throw new CustomException("Property must be approved before toggling status.", HttpStatus.BAD_REQUEST);
        }

        PropertyStatus next = (property.getStatus() == PropertyStatus.ACTIVE ||
                               property.getStatus() == PropertyStatus.APPROVED)
                ? PropertyStatus.INACTIVE
                : PropertyStatus.ACTIVE;

        property.setStatus(next);
        Property saved = propertyRepository.save(property);
        log.info("Owner {} toggled property id={} → {}", ownerEmail, propertyId, next);
        return OwnerPropertyDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerPropertyDto resubmitProperty(String ownerEmail, Long propertyId) {
        Property property = resolveOwnedProperty(ownerEmail, propertyId);

        if (property.getStatus() != PropertyStatus.REJECTED) {
            throw new CustomException("Only rejected properties can be resubmitted.", HttpStatus.BAD_REQUEST);
        }

        property.setStatus(PropertyStatus.PENDING);
        property.setRejectionReason(null);
        property.setSubmittedAt(java.time.LocalDateTime.now());
        Property saved = propertyRepository.save(property);

        adminNotificationService.createNotification(
            "Property Resubmitted for Review",
            "Property '" + saved.getName() + "' has been updated and resubmitted by the owner.",
            com.b4code.backend.models.enums.AdminNotificationType.NEW_PROPERTY,
            saved.getId().toString()
        );

        log.info("Owner {} resubmitted property id={}", ownerEmail, propertyId);
        return OwnerPropertyDto.fromEntity(saved);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private User resolveOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Owner not found: " + email, HttpStatus.NOT_FOUND));
    }

    private Property resolveOwnedProperty(String ownerEmail, Long propertyId) {
        User owner = resolveOwner(ownerEmail);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found: " + propertyId, HttpStatus.NOT_FOUND));
        if (!owner.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied: property does not belong to this owner.", HttpStatus.FORBIDDEN);
        }
        return property;
    }

    private void attachAmenities(Property property, List<String> names) {
        if (names == null || names.isEmpty()) return;
        if (property.getAmenities() == null) property.setAmenities(new HashSet<>());
        for (String name : names) {
            Amenity amenity = Amenity.builder().name(name).property(property).build();
            property.getAmenities().add(amenity);
        }
    }
}
