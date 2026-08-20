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
@RequestMapping("/api/staff/notifications")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
@RequiredArgsConstructor
@Tag(name = "Staff - Notifications")
public class StaffNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        return ResponseEntity.ok(notificationService.getUserNotifications());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Integer> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
