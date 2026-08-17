package com.b4code.backend.service.impl;

import com.b4code.backend.dto.NotificationDto;
import com.b4code.backend.models.Notification;
import com.b4code.backend.models.User;
import com.b4code.backend.dao.NotificationRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications() {
        User user = getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        User user = getCurrentUser();
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void createNotification(User user, String title, String message) {
        if (user == null) return;
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", user.getEmail(), title);
    }
}
