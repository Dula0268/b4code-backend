package com.b4code.backend.rest;

import com.b4code.backend.service.IcalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/ical")
@RequiredArgsConstructor
@Tag(name = "Public iCal", description = "Public endpoints for exporting iCalendar (.ics) feeds to Airbnb, Booking.com, and Google Calendar")
public class PublicIcalController {

    private final IcalService icalService;

    @GetMapping(value = "/properties/{propertyId}/calendar.ics", produces = "text/calendar")
    @Operation(summary = "Export entire property calendar in RFC-5545 iCalendar (.ics) format")
    public ResponseEntity<String> exportPropertyCalendar(@PathVariable Long propertyId) {
        String ics = icalService.generateIcsFeed(propertyId, null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"property-" + propertyId + ".ics\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .body(ics);
    }

    @GetMapping(value = "/properties/{propertyId}/rooms/{roomTypeId}/calendar.ics", produces = "text/calendar")
    @Operation(summary = "Export room-specific calendar in RFC-5545 iCalendar (.ics) format")
    public ResponseEntity<String> exportRoomCalendar(
            @PathVariable Long propertyId,
            @PathVariable Long roomTypeId) {
        String ics = icalService.generateIcsFeed(propertyId, roomTypeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"room-" + roomTypeId + ".ics\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .body(ics);
    }
}
