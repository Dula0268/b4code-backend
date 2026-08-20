package com.b4code.backend.service;

import com.b4code.backend.dto.PropertyDto;
import com.b4code.backend.dto.PropertyPageDto;
import com.b4code.backend.dto.PropertyRejectionDto;
import com.b4code.backend.dto.PropertySimpleDto;
import com.b4code.backend.models.enums.PropertyStatus;
import java.util.List;

public interface PropertyService {

    PropertyPageDto getAllProperties(String search, PropertyStatus status, int page, int size);

    PropertyDto getPropertyById(Long id);

    PropertyDto createProperty(PropertyDto dto);

    PropertyDto approveProperty(Long id);

    PropertyDto rejectProperty(Long id, PropertyRejectionDto rejection);

    PropertyDto markUnderReview(Long id);

    List<PropertySimpleDto> getPublicPropertiesList();
}
