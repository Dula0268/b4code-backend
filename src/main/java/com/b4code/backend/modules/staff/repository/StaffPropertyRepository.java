package com.b4code.backend.modules.staff.repository;

import com.b4code.backend.modules.staff.entity.StaffProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StaffPropertyRepository extends JpaRepository<StaffProperty, Long> {
    List<StaffProperty> findByStaffId(Long staffId);
    List<StaffProperty> findByPropertyId(Long propertyId);
    Optional<StaffProperty> findByStaffIdAndPropertyId(Long staffId, Long propertyId);
    
    @Transactional
    void deleteByStaffIdAndPropertyId(Long staffId, Long propertyId);
}
