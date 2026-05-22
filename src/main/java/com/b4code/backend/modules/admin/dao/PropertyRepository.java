package com.b4code.backend.modules.admin.dao;

import com.b4code.backend.models.enums.PropertyStatus;
import com.b4code.backend.models.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("""
            SELECT p FROM Property p
            WHERE p.pvId LIKE 'PV-%'
              AND (:status IS NULL OR p.status = :status)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(p.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.pvId)      LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.ownerName) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Property> findAllWithFilters(
            @Param("status") PropertyStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT p FROM Property p WHERE p.pvId LIKE 'PV-%' AND p.status IN :statuses ORDER BY p.submittedAt DESC LIMIT 5")
    List<Property> findTop5ByStatusInOrderBySubmittedAtDesc(@Param("statuses") List<PropertyStatus> statuses);

    List<Property> findByStatus(PropertyStatus status);
    
    List<Property> findByOwnerId(Long ownerId);

    long countByPvIdIsNotNull();
    long countByPvIdStartingWith(String prefix);
}
