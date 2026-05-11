package com.b4code.backend.modules.staff.repository;

import com.b4code.backend.modules.staff.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByPropertyId(Long propertyId);
    List<MenuItem> findByPropertyIdAndCategory(Long propertyId, String category);
    void deleteByPropertyIdAndCategory(Long propertyId, String category);
    java.util.Optional<MenuItem> findByName(String name);
}
