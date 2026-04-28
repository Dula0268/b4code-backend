package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.OwnerProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerPropertyRepository extends JpaRepository<OwnerProperty, Long> {

    List<OwnerProperty> findByOwnerId(Long ownerId);

    long countByOwnerId(Long ownerId);
}
