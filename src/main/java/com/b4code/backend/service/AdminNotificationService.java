package com.b4code.backend.service;

import com.b4code.backend.dto.AdminNotificationDto;
import com.b4code.backend.models.enums.AdminNotificationType;
import org.springframework.data.domain.Page;

public interface AdminNotificationService {
    void createNotification(String title, String message, AdminNotificationType type, String referenceId);
    Page<AdminNotificationDto> getRecentNotifications(int page, int size);
    void markAsRead(Long id);
    void markAllAsRead();
}
