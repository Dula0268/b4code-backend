package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.Integration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    List<Integration> findByOwnerIdOrderByNameAsc(Long ownerId);
}
