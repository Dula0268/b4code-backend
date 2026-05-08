package com.hospitality.dao;

import com.hospitality.enums.ReviewStatus;
import com.hospitality.models.FlaggedReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlaggedReviewRepository extends JpaRepository<FlaggedReview, Long> {

    @Query("""
        SELECT r FROM FlaggedReview r
        WHERE (CAST(:status AS string) IS NULL OR r.status = :status)
          AND (CAST(:flagReason AS string) IS NULL OR r.flagReason = :flagReason)
          AND (CAST(:search AS string) IS NULL OR :search = '' OR
               LOWER(r.guestName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(r.propertyName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(r.flagReason) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY r.flaggedAt DESC
        """)
    Page<FlaggedReview> findAllWithFilters(
            @Param("status") ReviewStatus status,
            @Param("flagReason") String flagReason,
            @Param("search") String search,
            Pageable pageable);

    long countByStatus(ReviewStatus status);
}
