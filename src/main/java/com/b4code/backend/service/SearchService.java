package com.b4code.backend.service;

import com.b4code.backend.dto.SearchDTO.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SearchService {

    PaginatedResponse<PropertySearchResult> search(
            String destination,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests,
            Integer rooms,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            List<String> amenities,
            String sortBy,
            int page,
            int size);

    PropertyDetailResult getPropertyDetail(Long propertyId, LocalDate checkIn, LocalDate checkOut);

    FilterOptionsResponse getFilterOptions();
}
