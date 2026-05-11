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

import com.b4code.backend.modules.admin.models.Transaction;
import com.b4code.backend.modules.admin.models.Refund;
import com.b4code.backend.modules.admin.models.Payout;
import com.b4code.backend.modules.admin.models.Property;
import com.b4code.backend.modules.admin.dao.PropertyRepository;
import com.b4code.backend.modules.staff.entity.MenuItem;
import com.b4code.backend.modules.staff.repository.MenuItemRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final FlaggedReviewRepository flaggedReviewRepository;
    private final DisputeRepository disputeRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionRepository transactionRepository;
    private final RefundRepository refundRepository;
    private final PayoutRepository payoutRepository;
    private final PropertyRepository propertyRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(UserRepository userRepository,
            AdminUserRepository adminUserRepository,
            FlaggedReviewRepository flaggedReviewRepository,
            DisputeRepository disputeRepository,
            MenuItemRepository menuItemRepository,
            PasswordEncoder passwordEncoder) {

    private final PropertyRepository propertyRepository;

    public DataSeeder(UserRepository userRepository,
            AdminUserRepository adminUserRepository,
            FlaggedReviewRepository flaggedReviewRepository,
            DisputeRepository disputeRepository,
            MenuItemRepository menuItemRepository,
            PasswordEncoder passwordEncoder,
            PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.flaggedReviewRepository = flaggedReviewRepository;
        this.disputeRepository = disputeRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionRepository = transactionRepository;
        this.refundRepository = refundRepository;
        this.payoutRepository = payoutRepository;
        this.propertyRepository = propertyRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void run(String... args) {

        // ✅ Ensure admin always exists
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

        // 3. Seed other users
        seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", User.Role.GUEST, null,
                User.UserStatus.ACTIVE);
        seedUserIfMissing("guest1@primestay.com", "guest123", "Alice", "Guest", User.Role.GUEST, 1L,
                User.UserStatus.ACTIVE);

        // Ensure guest1 has propertyId = 1 if already seeded
        userRepository.findByEmail("guest1@primestay.com").ifPresent(u -> {
            if (u.getPropertyId() == null || u.getPropertyId() != 1L) {
                u.setPropertyId(1L);
                userRepository.save(u);
                System.out.println("Updated guest1 propertyId to 1");
            }
        });

        seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", User.Role.OWNER, null,
                User.UserStatus.ACTIVE);

        // ✅ Specific Staff Login (Linked to Property 1 and APPROVED)
        seedUserIfMissing("staff@primestay.com", "staff123", "Mike", "Staff", User.Role.STAFF, 1L,
                User.UserStatus.APPROVED);

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
            seedFlaggedReview(101L, "Oceanview Villa", 201L, "Alice Smith", "AS", "blue",
                    "The place was a total mess and not as described. Bugs everywhere!", 1.5, "Inappropriate Content",
                    ReviewStatus.FLAGGED);
            seedFlaggedReview(102L, "Mountain Retreat", 202L, "Bob Jones", "BJ", "green",
                    "Host demanded extra cash upon arrival. Very shady.", 2.0, "Policy Violation",
                    ReviewStatus.FLAGGED);
            seedFlaggedReview(103L, "City Center Apartment", 203L, "Carol White", "CW", "purple",
                    "Great place, but the neighbors were a bit loud.", 4.0, "Spam", ReviewStatus.FLAGGED);
            System.out.println("✅ Flagged reviews seeded");
        }

        // ✅ Seed Disputes
        if (disputeRepository.count() == 0) {
            seedDispute("DSP-1001", 201L, "Alice Smith", 101L, "Oceanview Villa", "BKG-9901",
                    "Host cancelled last minute, requesting full refund.", new BigDecimal("15000.00"), "LKR",
                    "2026-06-01 to 2026-06-05", "Strict", 5, DisputeStatus.OPEN);
            seedDispute("DSP-1002", 204L, "David Brown", 104L, "Desert Oasis", "BKG-9902",
                    "Property amenities missing (no pool as advertised).", new BigDecimal("5000.00"), "LKR",
                    "2026-05-10 to 2026-05-12", "Moderate", 3, DisputeStatus.OPEN);
            System.out.println("✅ Disputes seeded");
        }

        // ✅ Seed/Update Menu Items for Property 1
        seedOrUpdateMenuItem(1L, "Classic Margherita Pizza", "Main",
                "Fresh mozzarella, basil, and tomato sauce on a thin crust.", new BigDecimal("2500.00"),
                java.util.List.of(
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485194/pro3e5jrllljbttvqsni.jpg",
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485195/v0tkfbvbokimxyjblsgc.jpg",
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485038/iknjlwvyxlusvpa6npex.jpg"));
        seedOrUpdateMenuItem(1L, "Sri Lankan Rice & Curry", "Main",
                "Authentic village-style rice and curry with chicken and assorted vegetables.",
                new BigDecimal("1800.00"),
                java.util.List.of(
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485192/tsjra56wpkcjjsralkdt.jpg",
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485194/t6e27scjzdoufdwqfvga.jpg"));
        seedOrUpdateMenuItem(1L, "Watalappam", "Dessert",
                "Traditional Sri Lankan coconut custard pudding with jaggery.", new BigDecimal("850.00"),
                java.util.List
                        .of("https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485193/quifhrtj1wg0mjgb5pya.jpg"));
        seedOrUpdateMenuItem(1L, "Fresh King Coconut", "Drink", "Chilled natural king coconut water.",
                new BigDecimal("450.00"),
                java.util.List.of(
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485190/fjoolp2br10pqp56u2t3.jpg",
                        "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485191/lk3whcfcoanysx611dnu.jpg"));
        System.out.println("✅ Menu items synced for Property 1");
    }

    private void seedUserIfMissing(String email, String password, String first, String last, User.Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    if (user.getRole() != role) {
                        user.setRole(role);
                        userRepository.save(user);
                        System.out.println("✅ Forcefully updated " + email + " to " + role + " role");
                    }
                },
                () -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setFirstName(first);
                    user.setLastName(last);
                    user.setRole(role);
                    user.setStatus(User.UserStatus.ACTIVE);
                    userRepository.save(user);
                    System.out.println("✅ Default " + role + " user created: " + email);
                });
    }

    private void seedAdminUser(String first, String last, String email, UserRole role, UserStatus status) {
        AdminUser u = new AdminUser();
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setStatus(status);
        adminUserRepository.save(u);
    }

    private void seedFlaggedReview(Long propertyId, String propertyName, Long guestId, String guestName,
            String guestInitial, String avatarColor, String reviewText, Double rating,
            String flagReason, ReviewStatus status) {
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

    private void seedDispute(String disputeId, Long guestId, String guestName, Long propertyId,
            String propertyName, String bookingId, String reason, BigDecimal amount,
            String currency, String stayDates, String cancellationPolicy,
            Integer daysUntilAutoClose, DisputeStatus status) {
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

    private void seedOrUpdateMenuItem(Long propertyId, String name, String category, String description,
            BigDecimal price, java.util.List<String> imageUrls) {
        menuItemRepository.findByName(name).ifPresentOrElse(
                item -> {
                    item.setCategory(category);
                    item.setDescription(description);
                    item.setPrice(price);
                    item.setImageUrls(imageUrls);
                    menuItemRepository.save(item);
                },
                () -> {
                    MenuItem item = new MenuItem();
                    item.setPropertyId(propertyId);
                    item.setName(name);
                    item.setCategory(category);
                    item.setDescription(description);
                    item.setPrice(price);
                    item.setIsAvailable(true);
                    item.setImageUrls(imageUrls);
                    menuItemRepository.save(item);
                });
    }
}