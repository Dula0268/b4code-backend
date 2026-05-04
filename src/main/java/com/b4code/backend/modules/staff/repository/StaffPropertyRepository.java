package com.b4code.backend.modules.staff.repository;

import com.b4code.backend.modules.staff.entity.StaffProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffPropertyRepository extends JpaRepository<StaffProperty, Long> {

    Optional<StaffProperty> findByStaffIdAndPropertyId(Long staffId, Long propertyId);

    List<StaffProperty> findByStaffId(Long staffId);

    void deleteByStaffIdAndPropertyId(Long staffId, Long propertyId);
}