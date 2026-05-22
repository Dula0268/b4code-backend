package com.b4code.backend.dao;

import com.b4code.backend.models.StaffProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffPropertyRepository extends JpaRepository<StaffProperty, Long> {

    Optional<StaffProperty> findByStaffIdAndPropertyId(Long staffId, Long propertyId);

    List<StaffProperty> findByStaffId(Long staffId);

    void deleteByStaffIdAndPropertyId(Long staffId, Long propertyId);
}
