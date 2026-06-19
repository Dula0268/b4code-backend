package com.b4code.backend.dao;

import com.b4code.backend.models.ModerationHistory;
import com.b4code.backend.models.enums.ModerationAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ModerationHistoryRepository extends JpaRepository<ModerationHistory, Long> {

    @Query("""
        SELECT h FROM ModerationHistory h
        LEFT JOIN h.admin a
        WHERE (CAST(:action AS string) IS NULL OR h.actionTaken = :action)
          AND (CAST(:from AS LocalDateTime) IS NULL OR h.resolvedAt >= :from)
          AND (CAST(:to AS LocalDateTime) IS NULL OR h.resolvedAt <= :to)
          AND (CAST(:search AS string) IS NULL OR :search = '' OR
               LOWER(h.caseId) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(a.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(a.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(h.outcome) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY h.resolvedAt DESC
        """)
    Page<ModerationHistory> findAllWithFilters(
            @Param("action") ModerationAction action,
            @Param("search") String search,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    long countByActionTakenAndResolvedAtAfter(ModerationAction actionTaken, LocalDateTime resolvedAt);
}
