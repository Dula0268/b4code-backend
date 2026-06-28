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
                .name(request.getName())
                .description(request.getDescription())
                .addressLine1(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .checkInTime(request.getCheckIn() != null ? request.getCheckIn() : "14:00")
                .checkOutTime(request.getCheckOut() != null ? request.getCheckOut() : "11:00")
                .houseRules(request.getHouseRules())
                .propertyType(request.getPropertyType())
                .imageUrl(request.getImageUrl())
                .status(PropertyStatus.PENDING)
                .build();

        attachAmenities(property, request.getAmenities());
        Property saved = propertyRepository.save(property);
        log.info("Owner {} created property id={}", ownerEmail, saved.getId());
        return OwnerPropertyDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public OwnerPropertyDto updateProperty(String ownerEmail, Long propertyId, OwnerPropertyRequest request) {
        Property property = resolveOwnedProperty(ownerEmail, propertyId);

        if (request.getName() != null)          property.setName(request.getName());
        if (request.getDescription() != null)   property.setDescription(request.getDescription());
        if (request.getAddress() != null)       property.setAddressLine1(request.getAddress());
        if (request.getCity() != null)          property.setCity(request.getCity());
        if (request.getCountry() != null)       property.setCountry(request.getCountry());
        if (request.getContactPhone() != null)  property.setContactPhone(request.getContactPhone());
        if (request.getContactEmail() != null)  property.setContactEmail(request.getContactEmail());
        if (request.getCheckIn() != null)       property.setCheckInTime(request.getCheckIn());
        if (request.getCheckOut() != null)      property.setCheckOutTime(request.getCheckOut());
        if (request.getHouseRules() != null)    property.setHouseRules(request.getHouseRules());
        if (request.getPropertyType() != null)  property.setPropertyType(request.getPropertyType());
        if (request.getImageUrl() != null)       property.setImageUrl(request.getImageUrl());

        if (request.getAmenities() != null) {
            property.getAmenities().clear();
            attachAmenities(property, request.getAmenities());
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
