package com.b4code.backend.dao;

import com.b4code.backend.models.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByPropertyId(Long propertyId, Pageable pageable);
    
    List<MenuItem> findByPropertyId(Long propertyId);
    
    @Query("SELECT m FROM MenuItem m WHERE m.menu.id = :menuId")
    List<MenuItem> findByMenuId(@Param("menuId") Long menuId);
    
    @Query("SELECT m FROM MenuItem m WHERE m.menu.id = :menuId AND m.category.id = :categoryId")
    List<MenuItem> findByMenuIdAndCategoryId(@Param("menuId") Long menuId, @Param("categoryId") Long categoryId);
    
    @Modifying
    @Query("DELETE FROM MenuItem m WHERE m.menu.id = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);

    @Modifying
    @Query("UPDATE MenuItem m SET m.menu = null WHERE m.menu.id = :menuId")
    void unlinkByMenuId(@Param("menuId") Long menuId);
}

