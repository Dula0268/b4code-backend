package com.b4code.backend.common.config;

import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import com.b4code.backend.modules.admin.models.AdminUser;
import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;
import com.b4code.backend.modules.admin.models.FlaggedReview;
import com.b4code.backend.modules.admin.models.Dispute;
import com.b4code.backend.modules.admin.dao.FlaggedReviewRepository;
import com.b4code.backend.modules.admin.dao.DisputeRepository;
import com.b4code.backend.modules.admin.enums.ReviewStatus;
import com.b4code.backend.modules.admin.enums.DisputeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final FlaggedReviewRepository flaggedReviewRepository;
    private final DisputeRepository disputeRepository;
    private final PasswordEncoder passwordEncoder;

    // Manual constructor to avoid Lombok @RequiredArgsConstructor issues
    public DataSeeder(UserRepository userRepository,
            AdminUserRepository adminUserRepository,
            FlaggedReviewRepository flaggedReviewRepository,
            DisputeRepository disputeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.flaggedReviewRepository = flaggedReviewRepository;
        this.disputeRepository = disputeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // ✅ Ensure admin always exists (dev logic)
        userRepository.findByEmail("admin@primestay.com").ifPresentOrElse(
                admin -> {
                    if (admin.getRole() != User.Role.ADMIN) {
                        admin.setRole(User.Role.ADMIN);
                        userRepository.save(admin);
                        System.out.println("✅ Updated admin role");
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setEmail("admin@primestay.com");
                    admin.setPasswordHash(passwordEncoder.encode("admin123"));
                    admin.setFirstName("System");
                    admin.setLastName("Admin");
                    admin.setRole(User.Role.ADMIN);
                    admin.setStatus(User.UserStatus.ACTIVE);
                    userRepository.save(admin);
                    System.out.println("✅ Admin user created");
                });

        // ✅ Your additional seed users
        seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", User.Role.GUEST);
        seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", User.Role.OWNER);
        seedUserIfMissing("staff@primestay.com", "staff123", "Mike", "Staff", User.Role.STAFF);

        // ✅ Admin users table
        if (adminUserRepository.count() == 0) {
            seedAdminUser("Sarah", "Jenkins", "sarah.j@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Mike", "Ross", "mike.ross@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("John", "Doe", "john.d@gmail.com", UserRole.STAFF, UserStatus.SUSPENDED);
            seedAdminUser("Emily", "Chen", "emily.chen@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Aisha", "Kumar", "aisha.k@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Nina", "Patel", "nina.patel@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Daniel", "Osei", "daniel.o@primestay.com", UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Priya", "Sharma", "priya.s@primestay.com", UserRole.OWNER, UserStatus.SUSPENDED);
            System.out.println("✅ Sample admin_users seeded (8 records)");
        }

        // ✅ Seed Flagged Reviews
        if (flaggedReviewRepository.count() == 0) {
            seedFlaggedReview(101L, "Oceanview Villa", 201L, "Alice Smith", "AS", "blue", "The place was a total mess and not as described. Bugs everywhere!", 1.5, "Inappropriate Content", ReviewStatus.FLAGGED);
            seedFlaggedReview(102L, "Mountain Retreat", 202L, "Bob Jones", "BJ", "green", "Host demanded extra cash upon arrival. Very shady.", 2.0, "Policy Violation", ReviewStatus.FLAGGED);
            seedFlaggedReview(103L, "City Center Apartment", 203L, "Carol White", "CW", "purple", "Great place, but the neighbors were a bit loud.", 4.0, "Spam", ReviewStatus.FLAGGED);
            System.out.println("✅ Flagged reviews seeded");
        }

        // ✅ Seed Disputes
        if (disputeRepository.count() == 0) {
            seedDispute("DSP-1001", 201L, "Alice Smith", 101L, "Oceanview Villa", "BKG-9901", "Host cancelled last minute, requesting full refund.", new BigDecimal("15000.00"), "LKR", "2026-06-01 to 2026-06-05", "Strict", 5, DisputeStatus.OPEN);
            seedDispute("DSP-1002", 204L, "David Brown", 104L, "Desert Oasis", "BKG-9902", "Property amenities missing (no pool as advertised).", new BigDecimal("5000.00"), "LKR", "2026-05-10 to 2026-05-12", "Moderate", 3, DisputeStatus.OPEN);
            System.out.println("✅ Disputes seeded");
        }
    }

    private void seedUserIfMissing(String email, String password, String first, String last, User.Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                // From 'dev' branch: Ensure the role is correct even if user already exists
                if (user.getRole() != role) {
                    user.setRole(role);
                    userRepository.save(user);
                    System.out.println("✅ Forcefully updated " + email + " to " + role + " role");
                }
            },
            () -> {
                // From 'feature' branch: Create new user
                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setFirstName(first);
                user.setLastName(last);
                user.setRole(role);
                user.setStatus(User.UserStatus.ACTIVE);
                userRepository.save(user);
                System.out.println("✅ Default " + role + " user created: " + email);
            }
        );
    }

    private void seedAdminUser(String first, String last, String email,
                               UserRole role, UserStatus status) {
        AdminUser u = new AdminUser();
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setStatus(status);
        adminUserRepository.save(u);
    }

    private void seedFlaggedReview(Long propertyId, String propertyName, Long guestId, String guestName, String guestInitial, String avatarColor, String reviewText, Double rating, String flagReason, ReviewStatus status) {
        FlaggedReview review = new FlaggedReview();
        review.setPropertyId(propertyId);
        review.setPropertyName(propertyName);
        review.setGuestId(guestId);
        review.setGuestName(guestName);
        review.setGuestInitial(guestInitial);
        review.setGuestAvatarColor(avatarColor);
        review.setReviewText(reviewText);
        review.setRating(rating);
        review.setFlagReason(flagReason);
        review.setStatus(status);
        review.setFlaggedAt(LocalDateTime.now().minusDays(1));
        flaggedReviewRepository.save(review);
    }

    private void seedDispute(String disputeId, Long guestId, String guestName, Long propertyId, String propertyName, String bookingId, String reason, BigDecimal amount, String currency, String stayDates, String cancellationPolicy, Integer daysUntilAutoClose, DisputeStatus status) {
        Dispute dispute = new Dispute();
        dispute.setDisputeId(disputeId);
        dispute.setGuestId(guestId);
        dispute.setGuestName(guestName);
        dispute.setPropertyId(propertyId);
        dispute.setPropertyName(propertyName);
        dispute.setBookingId(bookingId);
        dispute.setReason(reason);
        dispute.setAmount(amount);
        dispute.setCurrency(currency);
        dispute.setStayDates(stayDates);
        dispute.setCancellationPolicy(cancellationPolicy);
        dispute.setDaysUntilAutoClose(daysUntilAutoClose);
        dispute.setStatus(status);
        dispute.setOpenedAt(LocalDateTime.now().minusDays(2));
        disputeRepository.save(dispute);
    }
}
