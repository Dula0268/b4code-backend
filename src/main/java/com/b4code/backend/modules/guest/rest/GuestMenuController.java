package com.b4code.backend.modules.guest.rest;

import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/order")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GuestMenuController {

    private final MenuItemRepository menuItemRepository;

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItem>> getGuestMenu(
            @RequestParam Long propertyId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) String roomNumber) {
        
        log.info("Guest fetching menu for property: {}, table: {}, room: {}", propertyId, tableId, roomNumber);
        
        // For now, we just return all items for the property.
        // In the future, we could filter by availability or specific menu versions for tables/rooms.
        List<MenuItem> items = menuItemRepository.findByPropertyId(propertyId);
        
        log.debug("Found {} items for property {}", items.size(), propertyId);
        return ResponseEntity.ok(items);
    }
}
