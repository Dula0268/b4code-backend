package com.b4code.backend.service;

import com.b4code.backend.dto.NotificationDto;
import com.b4code.backend.models.User;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getUserNotifications();
    int getUnreadCount();
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void createNotification(User user, String title, String message);
}
