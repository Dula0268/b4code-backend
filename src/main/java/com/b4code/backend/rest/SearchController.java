package com.b4code.backend.rest;

import com.b4code.backend.dto.SearchDTO.*;
import com.b4code.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    /**
     * GET /api/guest/properties
     * Paginated property search with filters.
     */
    @GetMapping("/properties")
    public ResponseEntity<PaginatedResponse<PropertySearchResult>> searchProperties(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") Integer guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) List<String> propertyTypes,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(defaultValue = "recommended") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("GET /api/guest/properties - destination={}, page={}, size={}", destination, page, size);

        PaginatedResponse<PropertySearchResult> results = searchService.search(
                destination, checkIn, checkOut, guests,
                minPrice, maxPrice, minRating,
                propertyTypes, amenities, sortBy, page, size);

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/guest/properties/{id}
     * Property detail.
     */
    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<PropertyDetailResult> getPropertyDetail(@PathVariable Long propertyId) {
        log.info("GET /api/guest/properties/{}", propertyId);
        return ResponseEntity.ok(searchService.getPropertyDetail(propertyId));
    }

    /**
     * GET /api/guest/search/filters
     * Dynamic filter options for the search sidebar.
     */
    @GetMapping("/search/filters")
    public ResponseEntity<FilterOptionsResponse> getFilterOptions() {
        log.info("GET /api/guest/search/filters");
        return ResponseEntity.ok(searchService.getFilterOptions());
    }

    /**
     * Global exception handler for this controller.
     */
    @ExceptionHandler(com.b4code.backend.exceptions.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            com.b4code.backend.exceptions.ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(Map.of(
                "error", "Not Found",
                "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Unexpected error in SearchController", ex);
        return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", "An unexpected error occurred"));
    }
}
