package com.b4code.backend.dao;

import com.b4code.backend.models.Dispute;
import com.b4code.backend.models.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    @Query("""
            SELECT d FROM Dispute d
            WHERE (CAST(:status AS string) IS NULL OR d.status = :status)
              AND (:isComplaint IS NULL OR 
                   (:isComplaint = true AND d.amount = 0) OR 
                   (:isComplaint = false AND d.amount > 0))
              AND (CAST(:search AS string) IS NULL OR :search = '' OR
                   LOWER(d.guest.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(d.guest.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(d.property.name) LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(d.disputeId) LIKE LOWER(CONCAT('%',:search,'%')))
            ORDER BY d.openedAt DESC
            """)
    Page<Dispute> findAllWithFilters(
            @Param("status") DisputeStatus status,
            @Param("search") String search,
            @Param("isComplaint") Boolean isComplaint,
            Pageable pageable);

    long countByStatusNot(DisputeStatus status);

    long countByStatus(DisputeStatus status);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Dispute d WHERE d.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") DisputeStatus status);

    java.util.Optional<Dispute> findTopByBookingIdOrderByOpenedAtDesc(Long bookingId);
}
