package com.b4code.backend.dao;

import com.b4code.backend.models.Refund;
import com.b4code.backend.models.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("""
            SELECT r FROM Refund r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:resolved IS NULL OR (:resolved = true AND r.status IN ('APPROVED', 'REJECTED')) OR (:resolved = false AND r.status = 'PENDING'))
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(r.user.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.user.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.reason)   LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Refund> findAllWithFilters(
            @Param("status") RefundStatus status,
            @Param("resolved") Boolean resolved,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.status = 'APPROVED'")
    BigDecimal sumApprovedRefunds();

    @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = 'PENDING'")
    Long countPendingRefunds();
}
