package com.b4code.backend.rest.staff;

import com.b4code.backend.models.enums.FlagType;
import com.b4code.backend.models.enums.ReviewStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StaffReviewController {

    private final JdbcTemplate jdbcTemplate;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getStaffReviews(@RequestParam Long propertyId) {
        String sql = """
            SELECT ir.id, ir.menu_item_id, mi.name as menu_item_name, ir.guest_name, ir.rating, ir.comment, ir.created_at,
                   fr.status as flagged_status, fr.admin_note
            FROM staff.item_reviews ir
            JOIN staff.orders o ON o.id = ir.order_id
            JOIN staff.menu_items mi ON mi.id = ir.menu_item_id
            LEFT JOIN admin.flagged_reviews fr ON fr.review_id = ir.id AND fr.property_id = o.property_id
            WHERE o.property_id = ?
            ORDER BY ir.created_at DESC
        """;
        
        List<Map<String, Object>> reviews = jdbcTemplate.queryForList(sql, propertyId);
        return ResponseEntity.ok(reviews);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PostMapping("/{reviewId}/flag")
    public ResponseEntity<String> flagReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> body) {
            
        Long propertyId = ((Number) body.get("propertyId")).longValue();
        String flagTypeStr = (String) body.get("flagType");
        String flagReason = (String) body.get("flagReason");
        String reviewText = (String) body.get("reviewText");
        String guestName = (String) body.get("guestName");
        Double rating = ((Number) body.get("rating")).doubleValue();
        Long ownerId = 1L; // Mock owner ID or get from context

        String sql = """
            INSERT INTO admin.flagged_reviews (
                flag_type, status, flagged_at, updated_at, 
                owner_id, review_id, flag_reason, guest_name, 
                property_id, rating, review_text
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql, 
            flagTypeStr,
            ReviewStatus.FLAGGED.name(),
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now()),
            ownerId,
            reviewId,
            flagReason,
            guestName,
            propertyId,
            rating,
            reviewText
        );

        return ResponseEntity.ok("Review flagged successfully");
    }
}
