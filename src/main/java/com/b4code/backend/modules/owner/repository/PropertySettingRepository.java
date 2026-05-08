package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.PropertySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertySettingRepository extends JpaRepository<PropertySetting, Long> {

    Optional<PropertySetting> findByOwnerId(Long ownerId);
}
