package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByOwnerId(Long ownerId);
}
