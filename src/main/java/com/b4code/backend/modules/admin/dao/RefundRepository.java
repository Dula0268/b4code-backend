package com.b4code.backend.modules.admin.dao;

import com.b4code.backend.models.enums.RefundStatus;
import com.b4code.backend.models.Refund;
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
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(r.userName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.reason)   LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Refund> findAllWithFilters(
            @Param("status") RefundStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.status = 'APPROVED'")
    BigDecimal sumApprovedRefunds();
}
