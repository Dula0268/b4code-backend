package com.b4code.backend.rest;

import com.b4code.backend.dao.BookingRepository;
import com.b4code.backend.dto.PropertyDto;
import com.b4code.backend.dto.PropertySimpleDto;
import com.b4code.backend.dto.RoomStatusDto;
import com.b4code.backend.models.Booking;
import com.b4code.backend.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/properties/public")
@RequiredArgsConstructor
@Tag(name = "Public — Property", description = "Public property information")
public class PublicPropertyController {

    private final PropertyService propertyService;
    private final BookingRepository bookingRepository;

    @GetMapping("/{propertyId}/room-status")
    @Operation(summary = "Check whether a scanned room QR's room number is currently checked in",
            description = "Unauthenticated — used by the guest ordering flow to gate room-charge ordering behind a real front-desk check-in, and to show a live status badge in the ordering navbar.")
    public ResponseEntity<RoomStatusDto> getRoomStatus(@PathVariable Long propertyId, @RequestParam String roomNumber) {
        return bookingRepository
                .findByPropertyIdAndRoomNumberIgnoreCaseAndStatus(propertyId, roomNumber.trim(), Booking.BookingStatus.CHECKED_IN)
                .map(b -> ResponseEntity.ok(RoomStatusDto.builder()
                        .checkedIn(true)
                        .roomNumber(b.getRoomNumber())
                        .roomTypeName(b.getRoomType() != null ? b.getRoomType().getName() : null)
                        .guestName(b.getGuestName())
                        .checkOutDate(b.getCheckOut() != null ? b.getCheckOut().toString() : null)
                        .build()))
                .orElseGet(() -> ResponseEntity.ok(RoomStatusDto.builder()
                        .checkedIn(false)
                        .roomNumber(roomNumber.trim())
                        .build()));
    }

    @GetMapping("/list")
    @Operation(summary = "Get list of all approved properties (names and IDs only)")
    public ResponseEntity<List<PropertySimpleDto>> getPropertiesList() {
        return ResponseEntity.ok(propertyService.getPublicPropertiesList());
    }

    @GetMapping("/{id}/service-charge")
    @Operation(summary = "Get the service charge rate for a property")
    public ResponseEntity<java.util.Map<String, Double>> getServiceChargeRate(@org.springframework.web.bind.annotation.PathVariable Long id) {
        PropertyDto property = propertyService.getPropertyById(id);
        Double rate = property.getServiceChargeRate() != null ? property.getServiceChargeRate() : 10.0;
        return ResponseEntity.ok(java.util.Map.of("serviceChargeRate", rate));
    }

    @GetMapping("/{id}/charges")
    @Operation(summary = "Get all charges (service charge, tax) for a property")
    public ResponseEntity<java.util.Map<String, Double>> getCharges(@org.springframework.web.bind.annotation.PathVariable Long id) {
        PropertyDto property = propertyService.getPropertyById(id);
        Double serviceRate = property.getServiceChargeRate() != null ? property.getServiceChargeRate() : 10.0;
        return ResponseEntity.ok(java.util.Map.of(
            "serviceChargeRate", serviceRate,
            "taxRate", 5.0 // Keeping tax fixed for now as requested
        ));
    }
}




