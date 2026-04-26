package com.b4code.backend.modules.admin.service.impl;

import com.b4code.backend.modules.admin.dao.PropertyRepository;
import com.b4code.backend.modules.admin.dto.PropertyDto;
import com.b4code.backend.modules.admin.dto.PropertyPageDto;
import com.b4code.backend.modules.admin.dto.PropertyRejectionDto;
import com.b4code.backend.modules.admin.enums.PropertyStatus;
import com.b4code.backend.modules.admin.exceptions.CustomException;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.admin.service.PropertyService;
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

    // ── GET ALL (paginated + filtered)
    @Override
    @Transactional(readOnly = true)
    public PropertyPageDto getAllProperties(String search, PropertyStatus status, int page, int size) {
        log.debug("Fetching properties — search='{}', status={}, page={}, size={}", search, status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();

        Page<Property> pageResult = propertyRepository.findAllWithFilters(status, searchTerm, pageable);

        List<PropertyDto> content = pageResult.getContent()
                .stream()
                .map(PropertyDto::fromEntity)
                .toList();

        return PropertyPageDto.builder()
                .content(content)
                .currentPage(pageResult.getNumber())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .pageSize(pageResult.getSize())
                .build();
    }

    // ── GET SINGLE
    @Override
    @Transactional(readOnly = true)
    public PropertyDto getPropertyById(Long id) {
        return PropertyDto.fromEntity(findOrThrow(id));
    }

    // ── CREATE
    @Override
    @Transactional
    public PropertyDto createProperty(PropertyDto dto) {
        Property saved = propertyRepository.save(dto.toEntity());
        log.info("Property created — id={}, pvId={}", saved.getId(), saved.getPvId());
        return PropertyDto.fromEntity(saved);
    }

    // ── APPROVE
    @Override
    @Transactional
    public PropertyDto approveProperty(Long id) {
        Property property = findOrThrow(id);
        property.setStatus(PropertyStatus.APPROVED);
        property.setRejectionReason(null);
        log.info("Property id={} APPROVED", id);
        return PropertyDto.fromEntity(propertyRepository.save(property));
    }

    // ── REJECT
    @Override
    @Transactional
    public PropertyDto rejectProperty(Long id, PropertyRejectionDto rejection) {
        Property property = findOrThrow(id);
        property.setStatus(PropertyStatus.REJECTED);
        property.setRejectionReason(rejection.getReason());
        log.info("Property id={} REJECTED — reason='{}'", id, rejection.getReason());
        return PropertyDto.fromEntity(propertyRepository.save(property));
    }

    // ── MARK UNDER REVIEW
    @Override
    @Transactional
    public PropertyDto markUnderReview(Long id) {
        Property property = findOrThrow(id);
        property.setStatus(PropertyStatus.UNDER_REVIEW);
        log.info("Property id={} moved to UNDER_REVIEW", id);
        return PropertyDto.fromEntity(propertyRepository.save(property));
    }

    // ── Private helper
    private Property findOrThrow(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Property with id=" + id + " not found.", HttpStatus.NOT_FOUND));
    }
}
