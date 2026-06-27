package com.b4code.backend.dao;

import com.b4code.backend.models.ItemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {
    List<ItemReview> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);
    List<ItemReview> findByOrderId(Long orderId);
    boolean existsByOrderIdAndMenuItemId(Long orderId, Long menuItemId);
}
