package com.b4code.backend.service.impl;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dto.PropertyDto;
import com.b4code.backend.dto.PropertyPageDto;
import com.b4code.backend.dto.PropertyRejectionDto;
import com.b4code.backend.dto.PropertySimpleDto;
import com.b4code.backend.models.enums.PropertyStatus;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.models.Image;
import com.b4code.backend.models.ImageType;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.service.AdminNotificationService;
import com.b4code.backend.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AdminNotificationService adminNotificationService;

    @Override
    @Transactional(readOnly = true)
    public PropertyPageDto getAllProperties(String search, PropertyStatus status, int page, int size) {
        log.debug("Fetching properties — search='{}', status={}, page={}, size={}", search, status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();
        Page<Property> pageResult = propertyRepository.findAllWithFilters(searchTerm, status, pageable);
        List<PropertyDto> content = pageResult.getContent().stream().map(this::convertToDto).toList();
        return PropertyPageDto.builder()
                .content(content)
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDto getPropertyById(Long id) {
        return convertToDto(findOrThrow(id));
    }

    @Override
    @Transactional
    public PropertyDto createProperty(PropertyDto dto) {
        Property saved = propertyRepository.save(dto.toEntity());
        log.info("Property created — id={}", saved.getId());
        
        // Notify Admin
        adminNotificationService.createNotification(
            "New Property Registration",
            "A new property '" + saved.getName() + "' requires verification.",
            com.b4code.backend.models.enums.AdminNotificationType.NEW_PROPERTY,
            saved.getId().toString()
        );

        return convertToDto(saved);
    }

    @Override
    @Transactional
    public PropertyDto approveProperty(Long id) {
        Property property = findOrThrow(id);
        property.setStatus(PropertyStatus.APPROVED);
        log.info("Property id={} APPROVED (Mock)", id);
        return convertToDto(propertyRepository.save(property));
    }

    @Override
    @Transactional
    public PropertyDto rejectProperty(Long id, PropertyRejectionDto rejection) {
        Property property = findOrThrow(id);
        property.setStatus(PropertyStatus.REJECTED);
        log.info("Property id={} REJECTED (Mock) — reason='{}'", id, rejection.getReason());
        return convertToDto(propertyRepository.save(property));
    }

    @Override
    @Transactional
    public PropertyDto markUnderReview(Long id) {
        Property property = findOrThrow(id);
        property.setStatus(PropertyStatus.UNDER_REVIEW);
        log.info("Property id={} moved to UNDER_REVIEW (Mock)", id);
        return convertToDto(propertyRepository.save(property));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertySimpleDto> getPublicPropertiesList() {
        return propertyRepository.findAll()
                .stream()
                .map(p -> PropertySimpleDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .build())
                .toList();
    }

    private Property findOrThrow(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Property with id=" + id + " not found.", HttpStatus.NOT_FOUND));
    }

    private PropertyDto convertToDto(Property p) {
        PropertyDto dto = PropertyDto.fromEntity(p);
        
        if (p.getOwnerId() != null) {
            userRepository.findById(p.getOwnerId()).ifPresent(user -> {
                dto.setOwnerName(user.getFirstName() + " " + user.getLastName());
            });
        }
        
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            p.getImages().stream()
                .filter(img -> img.getType() == ImageType.PROPERTY)
                .findFirst()
                .ifPresent(img -> dto.setMainImageUrl(img.getUrl()));
        }
        
        return dto;
    }
}
