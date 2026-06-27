package com.b4code.backend.service;

import com.b4code.backend.dto.owner.OwnerPropertyDto;
import com.b4code.backend.dto.owner.OwnerPropertyPageDto;
import com.b4code.backend.dto.owner.OwnerPropertyRequest;

public interface OwnerPropertyService {

    OwnerPropertyPageDto listProperties(String ownerEmail, int page, int size, String search, String status);

    OwnerPropertyDto getProperty(String ownerEmail, Long propertyId);

    OwnerPropertyDto createProperty(String ownerEmail, OwnerPropertyRequest request);

    OwnerPropertyDto updateProperty(String ownerEmail, Long propertyId, OwnerPropertyRequest request);

    void deleteProperty(String ownerEmail, Long propertyId);

    OwnerPropertyDto toggleStatus(String ownerEmail, Long propertyId);
}
