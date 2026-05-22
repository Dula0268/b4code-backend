package com.b4code.backend.dao;

import com.b4code.backend.models.Transaction;
import com.b4code.backend.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @Query("""
      SELECT t FROM Transaction t
      WHERE (CAST(:type AS string) IS NULL OR t.type = :type)
        AND (CAST(:from AS LocalDateTime) IS NULL OR t.createdAt >= :from)
        AND (CAST(:to AS LocalDateTime) IS NULL OR t.createdAt <= :to)
        AND (
              CAST(:search AS string) IS NULL OR :search = ''
              OR LOWER(t.referenceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(t.userName)        LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(t.propertyName)    LIKE LOWER(CONCAT('%', :search, '%'))
            )
      """)
  Page<Transaction> findAllWithFilters(
      @Param("search") String search,
      @Param("type") TransactionType type,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);

  @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = com.b4code.backend.models.enums.TransactionType.BOOKING_PAYMENT")
  BigDecimal sumTotalRevenue();

  @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = com.b4code.backend.models.enums.TransactionType.COMMISSION")
  BigDecimal sumPlatformCommission();

  @Query(value = """
      SELECT TO_CHAR(t.created_at, 'Mon') AS month,
             SUM(t.amount) AS total
      FROM transactions t
      WHERE t.type = 'BOOKING_PAYMENT'
      GROUP BY TO_CHAR(t.created_at, 'Mon'),
               EXTRACT(MONTH FROM t.created_at)
      ORDER BY EXTRACT(MONTH FROM t.created_at)
      """, nativeQuery = true)
  java.util.List<Object[]> getMonthlyRevenue();

  default java.util.List<Object[]> getMonthlyRevenueTrend() {
    return getMonthlyRevenue();
  }
}
