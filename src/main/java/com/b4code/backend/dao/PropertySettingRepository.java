package com.b4code.backend.dao;

import com.b4code.backend.models.PropertySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertySettingRepository extends JpaRepository<PropertySetting, Long> {
    Optional<PropertySetting> findByPropertyId(Long propertyId);
}
