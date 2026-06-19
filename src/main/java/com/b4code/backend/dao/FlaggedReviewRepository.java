package com.b4code.backend.dao;

import com.b4code.backend.models.FlaggedReview;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.models.enums.FlagType;
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
            WHERE (:status IS NULL OR r.status = :status)
              AND (:flagType IS NULL OR r.flagType = :flagType)
              AND (:rating IS NULL OR r.review.overallRating = :rating)
              AND (:search IS NULL OR :search = '' OR
                   LOWER(r.review.guest.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(r.review.guest.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(r.review.property.name) LIKE LOWER(CONCAT('%',:search,'%')))
            """)
    Page<FlaggedReview> findAllWithFilters(
            @Param("status") ReviewStatus status,
            @Param("flagType") FlagType flagType,
            @Param("rating") Integer rating,
            @Param("search") String search,
            Pageable pageable);

    long countByStatus(ReviewStatus status);
}
