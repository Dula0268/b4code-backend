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

@RestController
@RequestMapping("/api/guest/order")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GuestMenuController {

    private final GuestMenuService guestMenuService;

    @GetMapping("/menu")
    public ResponseEntity<Page<MenuItemDto>> getGuestMenu(
            @RequestParam Long propertyId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) String roomNumber,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Guest fetching menu for property: {}, table: {}, room: {}, page: {}", 
                propertyId, tableId, roomNumber, pageable.getPageNumber());
        
        return ResponseEntity.ok(guestMenuService.getMenuForProperty(propertyId, pageable));
    }
}
