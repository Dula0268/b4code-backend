package com.b4code.backend.rest;

import com.b4code.backend.dto.ActiveOrderConversationDto;
import com.b4code.backend.dto.OrderMessageDto;
import com.b4code.backend.dto.OrderMessageRequest;
import com.b4code.backend.service.OrderMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Staff-facing endpoints for the order-guest messaging inbox. Only staff whose staffRole is
 * "Kitchen Staff" or "Staff Admin" may use these (enforced inside OrderMessageService) -
 * mirror image of StaffMessageController's "Property Staff"/"Staff Admin" gate.
 */
@RestController
@RequestMapping("/api/staff/order-messages")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class StaffOrderMessageController {

    private final OrderMessageService orderMessageService;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/property/{propertyId}/conversations")
    public ResponseEntity<List<ActiveOrderConversationDto>> getConversations(
            @PathVariable Long propertyId,
            Principal principal) {
        return ResponseEntity.ok(orderMessageService.getConversationsForProperty(propertyId, principal.getName()));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderMessageDto>> getMessages(
            @PathVariable Long orderId,
            Principal principal) {
        return ResponseEntity.ok(orderMessageService.getMessagesForStaff(orderId, principal.getName()));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PostMapping("/order/{orderId}")
    public ResponseEntity<OrderMessageDto> sendMessage(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderMessageRequest request,
            Principal principal) {
        return ResponseEntity.ok(orderMessageService.sendMessageAsStaff(orderId, principal.getName(), request.getContent()));
    }
}
