package com.b4code.backend.modules.admin.service;

import com.b4code.backend.modules.admin.dto.PropertyDto;
import com.b4code.backend.modules.admin.dto.PropertyPageDto;
import com.b4code.backend.modules.admin.dto.PropertyRejectionDto;
import com.b4code.backend.modules.admin.dto.PropertySimpleDto;
import com.b4code.backend.modules.admin.enums.PropertyStatus;
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
