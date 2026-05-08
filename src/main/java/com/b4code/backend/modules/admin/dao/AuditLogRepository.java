package com.b4code.backend.modules.admin.dao;

import com.b4code.backend.modules.admin.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (CAST(:role AS string) IS NULL OR a.userRole = :role)
          AND (CAST(:search AS string) IS NULL OR :search = '' OR
               LOWER(a.userName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(a.ipAddress) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(a.entity) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(a.entityDetail) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY a.timestamp DESC
        """)
    Page<AuditLog> findAllWithFilters(
            @Param("role") String role,
            @Param("search") String search,
            Pageable pageable);
}
