package com.b4code.backend.modules.owner.repository;

import com.b4code.backend.modules.owner.entity.PropertyMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyMediaRepository extends JpaRepository<PropertyMedia, Long> {

    List<PropertyMedia> findByPropertyIdOrderBySortOrderAsc(Long propertyId);

    void deleteByPropertyIdAndId(Long propertyId, Long mediaId);

    long countByPropertyId(Long propertyId);
}
