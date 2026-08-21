package com.b4code.backend.rest.staff;

import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.FlagType;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/reviews")
@RequiredArgsConstructor
@Slf4j
public class StaffReviewController {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final AdminNotificationService adminNotificationService;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getStaffReviews(@RequestParam Long propertyId) {
        String sql = """
            SELECT ir.id, ir.menu_item_id, mi.name as menu_item_name, ir.guest_name, ir.rating, ir.comment, ir.created_at,
                   fr.status as flagged_status, fr.admin_note
            FROM staff.item_reviews ir
            JOIN staff.orders o ON o.id = ir.order_id
            JOIN staff.menu_items mi ON mi.id = ir.menu_item_id
            LEFT JOIN (
                SELECT item_review_id, property_id, status, admin_note,
                       ROW_NUMBER() OVER(PARTITION BY item_review_id ORDER BY flagged_at DESC) as rn
                FROM admin.flagged_reviews
            ) fr ON fr.item_review_id = ir.id AND fr.property_id = o.property_id AND fr.rn = 1
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
            
        Object propObj = body.get("propertyId");
        Long propertyId = propObj != null ? ((Number) propObj).longValue() : 1L;
        
        String flagTypeStr = (String) body.get("flagType");
        String flagReason = (String) body.get("flagReason");
        String reviewText = (String) body.get("reviewText");
        String guestName = (String) body.get("guestName");
        Object ratingObj = body.get("rating");
        Double rating = ratingObj != null ? ((Number) ratingObj).doubleValue() : 0.0;
        
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication() != null 
            ? SecurityContextHolder.getContext().getAuthentication().getName() : null;

        Long ownerId = 1L; // Fallback
        if (currentUserEmail != null) {
            User staffUser = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (staffUser != null) {
                ownerId = staffUser.getId();
            }
        }

        String sql = """
            INSERT INTO admin.flagged_reviews (
                flag_type, status, flagged_at, updated_at, 
                owner_id, item_review_id, flag_reason, guest_name, 
                property_id, rating, review_text
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        final Long finalOwnerId = ownerId;
        
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, flagTypeStr);
            ps.setString(2, ReviewStatus.FLAGGED.name());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(5, finalOwnerId);
            ps.setLong(6, reviewId);
            ps.setString(7, flagReason);
            ps.setString(8, guestName);
            ps.setLong(9, propertyId);
            ps.setDouble(10, rating);
            ps.setString(11, reviewText);
            return ps;
        }, keyHolder);

        Long flaggedReviewId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : reviewId;

        // Notify Admin
        adminNotificationService.createNotification(
            "Review Flagged",
            "A review for property ID " + propertyId + " has been flagged by staff.",
            com.b4code.backend.models.enums.AdminNotificationType.FLAGGED_REVIEW,
            flaggedReviewId.toString()
        );

        return ResponseEntity.ok("Review flagged successfully");
    }
}
