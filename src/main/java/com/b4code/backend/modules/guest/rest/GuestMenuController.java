package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.guest.dto.MenuItemDto;
import com.b4code.backend.modules.guest.service.GuestMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/guest/order")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Guest: Menu", description = "Endpoints for guests to browse the property menu")
public class GuestMenuController {

    private final GuestMenuService guestMenuService;

    @GetMapping("/menu")
    @Operation(
        summary = "Get menu for guest", 
        description = "Returns a paginated list of menu items for a property, optionally filtered by context (table/room)",
        parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page number (0-indexed)", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Items per page", schema = @Schema(type = "integer", defaultValue = "20")),
            @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Sorting criteria (format: property,asc|desc)", schema = @Schema(type = "string", example = "name,asc"))
        }
    )
    public ResponseEntity<Page<MenuItemDto>> getGuestMenu(
            @RequestParam Long propertyId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) String roomNumber,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Guest fetching menu for property: {}, table: {}, room: {}, page: {}", 
                propertyId, tableId, roomNumber, pageable.getPageNumber());
        
        return ResponseEntity.ok(guestMenuService.getMenuForProperty(propertyId, pageable));
    }
}
