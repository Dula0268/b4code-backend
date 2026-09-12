package com.b4code.backend.dao;

import com.b4code.backend.models.PropertyIcalSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IcalSyncRepository extends JpaRepository<PropertyIcalSync, Long> {
    List<PropertyIcalSync> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    Optional<PropertyIcalSync> findByIdAndPropertyId(Long id, Long propertyId);
}
