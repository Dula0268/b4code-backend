package com.hospitality.service;

import com.hospitality.dto.admin.PropertyDto;
import com.hospitality.dto.admin.PropertyPageDto;
import com.hospitality.dto.admin.PropertyRejectionDto;
import com.hospitality.enums.PropertyStatus;

public interface PropertyService {

    PropertyPageDto getAllProperties(String search, PropertyStatus status, int page, int size);

    PropertyDto getPropertyById(Long id);

    PropertyDto createProperty(PropertyDto dto);

    PropertyDto approveProperty(Long id);

    PropertyDto rejectProperty(Long id, PropertyRejectionDto rejection);

    PropertyDto markUnderReview(Long id);
}
