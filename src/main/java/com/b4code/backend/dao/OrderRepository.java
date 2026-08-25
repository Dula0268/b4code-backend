package com.b4code.backend.dao;

import com.b4code.backend.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.b4code.backend.models.enums.OrderStatus;
import java.time.Instant;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, Pageable pageable);
    Page<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId, Pageable pageable);
    Page<Order> findByGuestIdAndGuestSessionIdOrderByCreatedAtDesc(Long guestId, String guestSessionId, Pageable pageable);
    Page<Order> findByGuestSessionIdOrderByCreatedAtDesc(String guestSessionId, Pageable pageable);
    List<Order> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    List<Order> findByGuestIdOrderByCreatedAtDesc(Long guestId);
    List<Order> findByGuestSessionIdOrderByCreatedAtDesc(String guestSessionId);

    @Query("SELECT o FROM Order o WHERE o.propertyId = :propertyId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR o.createdAt < :endDate) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findStaffOrders(@Param("propertyId") Long propertyId,
                                @Param("status") OrderStatus status,
                                @Param("startDate") Instant startDate,
                                @Param("endDate") Instant endDate,
                                Pageable pageable);

    /**
     * Same as {@link #findStaffOrders} but matches any of several statuses. The staff
     * order queue groups IN_PROGRESS and READY under a single "In-Progress" tab, so a
     * single-status filter cannot express what that tab needs.
     */
    @Query("SELECT o FROM Order o WHERE o.propertyId = :propertyId " +
           "AND o.status IN :statuses " +
           "AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR o.createdAt < :endDate) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findStaffOrdersByStatuses(@Param("propertyId") Long propertyId,
                                          @Param("statuses") Collection<OrderStatus> statuses,
                                          @Param("startDate") Instant startDate,
                                          @Param("endDate") Instant endDate,
                                          Pageable pageable);

    /** Total order count per status for a property, so paginated tabs can show real totals. */
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.propertyId = :propertyId GROUP BY o.status")
    List<Object[]> countOrdersByStatusForProperty(@Param("propertyId") Long propertyId);
}
