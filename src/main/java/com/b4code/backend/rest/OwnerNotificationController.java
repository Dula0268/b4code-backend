package com.b4code.backend.rest;

import com.b4code.backend.dto.NotificationDto;
import com.b4code.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/notifications")
@RequiredArgsConstructor
@Tag(name = "Owner - Notifications")
public class OwnerNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get owner notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        return ResponseEntity.ok(notificationService.getUserNotifications());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Integer> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
