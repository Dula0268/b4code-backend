package com.b4code.backend.dao;

import com.b4code.backend.models.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByPropertyId(Long propertyId, Pageable pageable);
    List<MenuItem> findByPropertyId(Long propertyId);
    List<MenuItem> findByPropertyIdAndCategory(Long propertyId, String category);
    Optional<MenuItem> findByName(String name);
    void deleteByPropertyIdAndCategory(Long propertyId, String category);
}
