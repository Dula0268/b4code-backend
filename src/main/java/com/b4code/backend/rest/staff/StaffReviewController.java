package com.b4code.backend.rest.staff;

import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.ReviewStatus;
import com.b4code.backend.models.enums.UserRole;
import com.b4code.backend.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/staff/reviews")
@RequiredArgsConstructor
@Slf4j
public class StaffReviewController {

    /** Mirrors OrderMessageService#assertKitchenStaffAccess's staffRole convention. */
    private static final Set<String> KITCHEN_STAFF_ROLES = Set.of("Kitchen Staff", "Staff Admin");
    /** Mirrors StaffMessageController#assertBookingInboxAccess's staffRole convention. */
    private static final Set<String> PROPERTY_STAFF_ROLES = Set.of("Property Staff", "Staff Admin");

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final AdminNotificationService adminNotificationService;

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getStaffReviews(@RequestParam Long propertyId, java.security.Principal principal) {
        assertKitchenReviewAccess(principal.getName());
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
            @RequestBody Map<String, Object> body,
            java.security.Principal principal) {
        assertKitchenReviewAccess(principal.getName());
        return doFlag(reviewId, body, "item_review_id");
    }

    /**
     * Booking/property reviews (guest.reviews), the counterpart to the item-review
     * endpoints above. Property Staff (front desk) see and flag these; Kitchen Staff
     * cannot. Staff Admin can access both review surfaces.
     */
    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @GetMapping("/booking")
    public ResponseEntity<List<Map<String, Object>>> getStaffBookingReviews(@RequestParam Long propertyId, java.security.Principal principal) {
        assertPropertyReviewAccess(principal.getName());
        String sql = """
            SELECT r.id, r.overall_rating as rating, r.comment, r.created_at,
                   CONCAT(u.first_name, ' ', u.last_name) as guest_name,
                   fr.status as flagged_status, fr.admin_note
            FROM guest.reviews r
            JOIN app_auth.users u ON u.id = r.guest_id
            LEFT JOIN (
                SELECT review_id, property_id, status, admin_note,
                       ROW_NUMBER() OVER(PARTITION BY review_id ORDER BY flagged_at DESC) as rn
                FROM admin.flagged_reviews
            ) fr ON fr.review_id = r.id AND fr.property_id = r.property_id AND fr.rn = 1
            WHERE r.property_id = ?
            ORDER BY r.created_at DESC
        """;

        List<Map<String, Object>> reviews = jdbcTemplate.queryForList(sql, propertyId);
        return ResponseEntity.ok(reviews);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'OWNER', 'ADMIN')")
    @PostMapping("/booking/{reviewId}/flag")
    public ResponseEntity<String> flagBookingReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> body,
            java.security.Principal principal) {
        assertPropertyReviewAccess(principal.getName());
        return doFlag(reviewId, body, "review_id");
    }

    /**
     * Shared insert for both review surfaces — only the FK column written
     * (item_review_id vs review_id) differs.
     */
    private ResponseEntity<String> doFlag(Long reviewId, Map<String, Object> body, String fkColumn) {
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
                owner_id, %s, flag_reason, guest_name,
                property_id, rating, review_text
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.formatted(fkColumn);

        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        final Long finalOwnerId = ownerId;

        jdbcTemplate.update(connection -> {
            // Ask Postgres to return ONLY the generated id. RETURN_GENERATED_KEYS makes
            // Postgres hand back every column of the new row, so KeyHolder#getKey() throws
            // ("current key entry contains multiple keys") even though the INSERT succeeded —
            // which surfaced to staff as "an error occurred" while still writing the flag row
            // (and piling up duplicates on every retry).
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
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

        // Resolve property name for a more informative notification
        String propertyName = propertyRepository.findById(propertyId)
                .map(Property::getName)
                .orElse("Property #" + propertyId);

        // Resolve staff member's display name
        String staffDisplayName = "Staff";
        if (currentUserEmail != null) {
            User staffUser = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (staffUser != null) {
                staffDisplayName = staffUser.getFirstName() + " " + staffUser.getLastName();
            }
        }

        // Notify Admin with property name and staff name included
        adminNotificationService.createNotification(
            "Review Flagged by Staff",
            staffDisplayName + " flagged a review for \"" + propertyName + "\". Please review it in the moderation queue.",
            com.b4code.backend.models.enums.AdminNotificationType.FLAGGED_REVIEW,
            flaggedReviewId.toString()
        );

        return ResponseEntity.ok("Review flagged successfully");
    }

    /** Kitchen Staff or Staff Admin only (STAFF-role accounts); OWNER/ADMIN bypass. */
    private void assertKitchenReviewAccess(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        if (user.getRole() == UserRole.STAFF
                && (user.getStaffRole() == null || !KITCHEN_STAFF_ROLES.contains(user.getStaffRole()))) {
            throw new CustomException("Item reviews are not available for your staff role", HttpStatus.FORBIDDEN);
        }
    }

    /** Property Staff or Staff Admin only (STAFF-role accounts); OWNER/ADMIN bypass. */
    private void assertPropertyReviewAccess(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        if (user.getRole() == UserRole.STAFF
                && (user.getStaffRole() == null || !PROPERTY_STAFF_ROLES.contains(user.getStaffRole()))) {
            throw new CustomException("Booking reviews are not available for your staff role", HttpStatus.FORBIDDEN);
        }
    }
}
