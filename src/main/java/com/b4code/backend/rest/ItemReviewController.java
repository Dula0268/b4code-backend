package com.b4code.backend.rest;

import com.b4code.backend.dao.ItemReviewRepository;
import com.b4code.backend.dao.OrderRepository;
import com.b4code.backend.models.ItemReview;
import com.b4code.backend.models.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ItemReviewController {

    private final OrderRepository orderRepository;
    private final ItemReviewRepository itemReviewRepository;

    @PostMapping("/orders/{orderId}/reviews")
    public ResponseEntity<?> submitReview(@PathVariable Long orderId, @RequestBody Map<String, Object> body) {
        log.info("Submitting review for order: {}", orderId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        }

        Order order = orderOpt.get();
        Long menuItemId = ((Number) body.get("menuItemId")).longValue();
        Integer rating = ((Number) body.get("rating")).intValue();
        String comment = (String) body.get("comment");
        String guestName = (String) body.get("guestName");

        if (itemReviewRepository.existsByOrderIdAndMenuItemId(orderId, menuItemId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Review already submitted for this item in this order"));
        }

        ItemReview review = ItemReview.builder()
                .menuItemId(menuItemId)
                .orderId(orderId)
                .guestName(guestName)
                .rating(rating)
                .comment(comment)
                .build();

        ItemReview saved = itemReviewRepository.save(review);
        log.info("Review saved with id: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/menu-items/{menuItemId}/reviews")
    public ResponseEntity<List<ItemReview>> getReviewsForMenuItem(@PathVariable Long menuItemId) {
        log.info("Fetching reviews for menu item: {}", menuItemId);
        List<ItemReview> reviews = itemReviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId);
        return ResponseEntity.ok(reviews);
    }
}
