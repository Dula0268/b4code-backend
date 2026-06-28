package com.b4code.backend.dao;

import com.b4code.backend.models.NotificationPref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPrefRepository extends JpaRepository<NotificationPref, Long> {
    Optional<NotificationPref> findByOwnerId(Long ownerId);
}
