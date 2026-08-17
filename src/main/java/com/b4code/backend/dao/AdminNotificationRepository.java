package com.b4code.backend.dao;

import com.b4code.backend.models.AdminNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    Page<AdminNotification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE AdminNotification n SET n.isRead = true WHERE n.isRead = false")
    int markAllAsRead();
}
