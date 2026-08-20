package com.b4code.backend.dao;

import com.b4code.backend.models.Payout;
import com.b4code.backend.models.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

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

    @Query("""
            SELECT p FROM Payout p
            WHERE p.ownerId = :ownerId
              AND p.status IN :statuses
              AND (
                    (p.status = 'PENDING' AND p.requestedAt >= :cutoff)
                    OR (p.status = 'PROCESSED' AND p.processedAt >= :cutoff)
              )
            ORDER BY p.requestedAt DESC
            """)
    List<Payout> findRecentActiveByOwnerIdOrderByRequestedAtDesc(
            @Param("ownerId") Long ownerId,
            @Param("statuses") java.util.Collection<com.b4code.backend.models.enums.PayoutStatus> statuses,
            @Param("cutoff") java.time.LocalDateTime cutoff
    );
}
