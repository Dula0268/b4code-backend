package com.b4code.backend.dao;

import com.b4code.backend.models.ItemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {
    List<ItemReview> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);
    List<ItemReview> findByOrderId(Long orderId);
    boolean existsByOrderIdAndMenuItemId(Long orderId, Long menuItemId);

    /** Batched avg rating + review count per item, for a whole property's menu in one query (avoids N+1). */
    @Query("SELECT r.menuItemId AS menuItemId, AVG(r.rating) AS avgRating, COUNT(r) AS reviewCount " +
           "FROM ItemReview r WHERE r.menuItemId IN :menuItemIds GROUP BY r.menuItemId")
    List<MenuItemRatingSummary> findRatingSummaryByMenuItemIds(@Param("menuItemIds") List<Long> menuItemIds);

    interface MenuItemRatingSummary {
        Long getMenuItemId();
        Double getAvgRating();
        Long getReviewCount();
    }
}
