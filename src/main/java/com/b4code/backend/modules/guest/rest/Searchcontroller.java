package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.guest.dto.SearchDTO.*;
import com.b4code.backend.modules.guest.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*") // adjust for prod
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/properties")
    public ResponseEntity<List<PropertySearchResult>> searchProperties(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") Integer guests,
            @RequestParam(defaultValue = "1") Integer rooms,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating) {

        SearchRequest request = SearchRequest.builder()
            .destination(destination)
            .checkIn(checkIn)
            .checkOut(checkOut)
            .guests(guests)
            .rooms(rooms)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minRating(minRating)
            .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<PropertyDetailResult> getPropertyDetail(@PathVariable Long propertyId) {
        return ResponseEntity.ok(searchService.getPropertyDetail(propertyId));
    }
}