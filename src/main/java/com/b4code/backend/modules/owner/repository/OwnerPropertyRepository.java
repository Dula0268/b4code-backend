package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.admin.models.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerPropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerId(Long ownerId);
}
