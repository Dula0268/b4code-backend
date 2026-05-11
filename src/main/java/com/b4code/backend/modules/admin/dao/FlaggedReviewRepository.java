package com.b4code.backend.modules.admin.dao;

import com.b4code.backend.modules.admin.enums.ReviewStatus;
import com.b4code.backend.modules.admin.models.FlaggedReview;
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
          AND (CAST(:flagReason AS string) IS NULL OR :flagReason = '' OR r.flagReason = :flagReason)
          AND (:rating IS NULL OR r.rating = :rating)
          AND (CAST(:search AS string) IS NULL OR :search = '' OR
               LOWER(r.guestName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(r.propertyName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(r.flagReason) LIKE LOWER(CONCAT('%',:search,'%')))
        """)
    Page<FlaggedReview> findAllWithFilters(
            @Param("status") ReviewStatus status,
            @Param("flagReason") String flagReason,
            @Param("rating") Double rating,
            @Param("search") String search,
            Pageable pageable);

    long countByStatus(ReviewStatus status);
}
