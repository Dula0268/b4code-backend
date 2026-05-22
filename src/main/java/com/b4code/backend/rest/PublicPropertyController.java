package com.b4code.backend.rest;

import com.b4code.backend.dto.PropertySimpleDto;
import com.b4code.backend.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/properties/public")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Public — Property", description = "Public property information")
public class PublicPropertyController {

    private final PropertyService propertyService;

    @GetMapping("/list")
    @Operation(summary = "Get list of all approved properties (names and IDs only)")
    public ResponseEntity<List<PropertySimpleDto>> getPropertiesList() {
        return ResponseEntity.ok(propertyService.getPublicPropertiesList());
    }
}




