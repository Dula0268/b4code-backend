package com.b4code.backend.modules.owner.service;

import com.b4code.backend.modules.owner.dto.PropertyDto.*;
import com.b4code.backend.modules.owner.entity.OwnerProperty;
import com.b4code.backend.modules.owner.entity.PropertyMedia;
import com.b4code.backend.modules.owner.repository.OwnerPropertyRepository;
import com.b4code.backend.modules.owner.repository.PropertyMediaRepository;
import com.b4code.backend.modules.owner.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final OwnerPropertyRepository propertyRepository;
    private final PropertyMediaRepository mediaRepository;
    private final RoomRepository roomRepository;

    /**
     * List all properties for an owner (paginated)
     */
    public PropertyListResponse listProperties(Long ownerId, int page, int size, String search, String statusFilter) {
        List<OwnerProperty> allProps = propertyRepository.findByOwnerId(ownerId);

        // Apply filters
        List<OwnerProperty> filtered = allProps.stream()
                .filter(p -> search == null || search.isEmpty()
                        || p.getName().toLowerCase().contains(search.toLowerCase())
                        || (p.getCity() != null && p.getCity().toLowerCase().contains(search.toLowerCase()))
                        || (p.getAddress() != null && p.getAddress().toLowerCase().contains(search.toLowerCase())))
                .filter(p -> statusFilter == null || "All".equals(statusFilter) || p.getStatus().equalsIgnoreCase(statusFilter))
                .collect(Collectors.toList());

        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = Math.min((page - 1) * size, totalItems);
        int end = Math.min(start + size, totalItems);

        List<PropertyListItem> items = filtered.subList(start, end).stream().map(p -> {
            PropertyListItem item = new PropertyListItem();
            item.setId(p.getId());
            item.setName(p.getName());
            item.setAddress((p.getAddress() != null ? p.getAddress() : "") + (p.getCity() != null ? ", " + p.getCity() : ""));
            item.setImage(p.getImageUrl());
            item.setRate(p.getRate() != null ? "Rs." + p.getRate().toPlainString() : "N/A");
            item.setRating(p.getRating() != null ? p.getRating() : 0.0);
            item.setReviews(p.getReviewCount() != null ? p.getReviewCount() : 0);
            item.setStatus(p.getStatus() != null ? p.getStatus().toLowerCase() : "inactive");
            item.setStatusOn("ACTIVE".equalsIgnoreCase(p.getStatus()));
            return item;
        }).collect(Collectors.toList());

        PropertyListResponse response = new PropertyListResponse();
        response.setProperties(items);
        response.setCurrentPage(page);
        response.setTotalPages(totalPages);
        response.setTotalItems(totalItems);
        return response;
    }

    /**
     * Get property details
     */
    public PropertyResponse getPropertyDetail(Long propertyId, Long ownerId) {
        OwnerProperty p = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        PropertyResponse resp = new PropertyResponse();
        resp.setId(p.getId());
        resp.setName(p.getName());
        resp.setDescription(p.getDescription());
        resp.setAddress(p.getAddress());
        resp.setCity(p.getCity());
        resp.setPostalCode(p.getPostalCode());
        resp.setPropertyType(p.getPropertyType());
        resp.setYearBuilt(p.getYearBuilt());
        resp.setContact(p.getContact());
        resp.setEmail(p.getEmail());
        resp.setCheckInTime(p.getCheckInTime());
        resp.setCheckOutTime(p.getCheckOutTime());
        resp.setRules(p.getRules());
        resp.setAmenities(p.getAmenities());
        resp.setImageUrl(p.getImageUrl());
        resp.setRate(p.getRate());
        resp.setRating(p.getRating());
        resp.setReviewCount(p.getReviewCount());
        resp.setStatus(p.getStatus());
        resp.setRoomCount(roomRepository.countByPropertyId(propertyId));
        return resp;
    }

    /**
     * Create a new property
     */
    @Transactional
    public PropertyResponse createProperty(Long ownerId, PropertyRequest request) {
        OwnerProperty p = new OwnerProperty();
        p.setOwnerId(ownerId);
        p.setPvId("PV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setAddress(request.getAddress());
        p.setCity(request.getCity());
        p.setContact(request.getContact());
        p.setEmail(request.getEmail());
        p.setCheckInTime(request.getCheckInTime());
        p.setCheckOutTime(request.getCheckOutTime());
        p.setRules(request.getRules());
        p.setStatus("ACTIVE");

        if (request.getAmenities() != null) {
            String amenityCsv = request.getAmenities().entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(","));
            p.setAmenities(amenityCsv);
        }

        OwnerProperty saved = propertyRepository.save(p);
        log.info("Property created: id={}, name={}", saved.getId(), saved.getName());
        return getPropertyDetail(saved.getId(), ownerId);
    }

    /**
     * Update an existing property
     */
    @Transactional
    public PropertyResponse updateProperty(Long propertyId, Long ownerId, PropertyRequest request) {
        OwnerProperty p = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        if (request.getName() != null) p.setName(request.getName());
        if (request.getDescription() != null) p.setDescription(request.getDescription());
        if (request.getAddress() != null) p.setAddress(request.getAddress());
        if (request.getCity() != null) p.setCity(request.getCity());
        if (request.getContact() != null) p.setContact(request.getContact());
        if (request.getEmail() != null) p.setEmail(request.getEmail());
        if (request.getCheckInTime() != null) p.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null) p.setCheckOutTime(request.getCheckOutTime());
        if (request.getRules() != null) p.setRules(request.getRules());

        if (request.getAmenities() != null) {
            String amenityCsv = request.getAmenities().entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(","));
            p.setAmenities(amenityCsv);
        }

        propertyRepository.save(p);
        log.info("Property updated: id={}", propertyId);
        return getPropertyDetail(propertyId, ownerId);
    }

    /**
     * Delete a property
     */
    @Transactional
    public void deleteProperty(Long propertyId, Long ownerId) {
        propertyRepository.deleteById(propertyId);
        log.info("Property deleted: id={}", propertyId);
    }

    /**
     * Toggle property status
     */
    @Transactional
    public PropertyResponse togglePropertyStatus(Long propertyId, Long ownerId) {
        OwnerProperty p = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        p.setStatus("ACTIVE".equalsIgnoreCase(p.getStatus()) ? "INACTIVE" : "ACTIVE");
        propertyRepository.save(p);
        return getPropertyDetail(propertyId, ownerId);
    }

    /**
     * List media for a property
     */
    public List<MediaResponse> listMedia(Long propertyId) {
        return mediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId).stream()
                .map(m -> {
                    MediaResponse mr = new MediaResponse();
                    mr.setId(m.getId());
                    mr.setUrl(m.getUrl());
                    mr.setFileName(m.getFileName());
                    mr.setMediaType(m.getMediaType());
                    mr.setIsPrimary(m.getIsPrimary());
                    return mr;
                }).collect(Collectors.toList());
    }
}
