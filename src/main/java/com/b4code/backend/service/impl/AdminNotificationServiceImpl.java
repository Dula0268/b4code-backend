package com.b4code.backend.service.impl;

import com.b4code.backend.dao.AdminNotificationRepository;
import com.b4code.backend.dto.AdminNotificationDto;
import com.b4code.backend.models.AdminNotification;
import com.b4code.backend.models.enums.AdminNotificationType;
import com.b4code.backend.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;

    @Override
    public void createNotification(String title, String message, AdminNotificationType type, String referenceId) {
        AdminNotification notification = AdminNotification.builder()
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        adminNotificationRepository.save(notification);
    }

    @Override
    public Page<AdminNotificationDto> getRecentNotifications(int page, int size) {
        return adminNotificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(AdminNotificationDto::fromEntity);
    }

    @Override
    public void markAsRead(Long id) {
        Optional<AdminNotification> opt = adminNotificationRepository.findById(id);
        if (opt.isPresent()) {
            AdminNotification n = opt.get();
            n.setRead(true);
            adminNotificationRepository.save(n);
        }
    }

    @Override
    public void markAllAsRead() {
        adminNotificationRepository.markAllAsRead();
    }
}
