package com.b4code.backend.modules.admin.dao;

import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.models.Payout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    @Query("""
            SELECT p FROM Payout p
            WHERE (:status IS NULL OR p.status = :status)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(p.ownerName) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Payout> findAllWithFilters(
            @Param("status") PayoutStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.status = 'PROCESSED'")
    BigDecimal sumProcessedPayouts();
}
