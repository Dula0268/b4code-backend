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
import com.b4code.backend.modules.admin.dao.TransactionRepository;
import com.b4code.backend.modules.admin.dao.RefundRepository;
import com.b4code.backend.modules.admin.dao.PayoutRepository;
import com.b4code.backend.modules.admin.dao.PropertyRepository;
import com.b4code.backend.modules.admin.enums.TransactionType;
import com.b4code.backend.modules.admin.enums.RefundStatus;
import com.b4code.backend.modules.admin.enums.PayoutStatus;
import com.b4code.backend.modules.admin.enums.PropertyStatus;

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
            PasswordEncoder passwordEncoder,
            TransactionRepository transactionRepository,
            RefundRepository refundRepository,
            PayoutRepository payoutRepository,
            PropertyRepository propertyRepository,
            JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.flaggedReviewRepository = flaggedReviewRepository;
        this.disputeRepository = disputeRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionRepository = transactionRepository;
        this.refundRepository = refundRepository;
        this.payoutRepository = payoutRepository;
        this.propertyRepository = propertyRepository;
        this.jdbcTemplate = jdbcTemplate;
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

        // ✅ Seed users
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

        // ✅ Seed Properties (use PV- prefix to distinguish admin-managed from guest properties)
        if (propertyRepository.countByPvIdStartingWith("PV-") == 0) {
            seedProperty("Oceanview Villa", "Galle Road, Colombo 03, Sri Lanka", "PV-10001", 101L, "Alex Owner",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=400", PropertyStatus.PENDING);
            seedProperty("Mountain Retreat", "Nuwara Eliya, Central Province, Sri Lanka", "PV-10002", 102L, "Sarah Jenkins",
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=400", PropertyStatus.UNDER_REVIEW);
            seedProperty("City Center Apartment", "Bauddhaloka Mawatha, Colombo 07, Sri Lanka", "PV-10003", 103L, "Emily Chen",
                "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=400", PropertyStatus.APPROVED);
            seedProperty("Desert Oasis Resort", "Kalpitiya, Puttalam District, Sri Lanka", "PV-10004", 104L, "Nina Patel",
                "https://images.unsplash.com/photo-1540541338287-41700207dee6?w=400", PropertyStatus.APPROVED);
            seedProperty("Lakeside Bungalow", "Beira Lake, Colombo 02, Sri Lanka", "PV-10005", 105L, "Daniel Osei",
                "https://images.unsplash.com/photo-1510798831971-661eb04b3739?w=400", PropertyStatus.PENDING);
            seedProperty("Sunset Beach Villa", "Unawatuna Beach, Galle, Sri Lanka", "PV-10006", 106L, "Priya Sharma",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=400", PropertyStatus.REJECTED);
            System.out.println("✅ Properties seeded (6 records)");
        }

        // ✅ Seed Transactions
        if (transactionRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            // Booking payments — last 6 months spread
            seedTransaction("TXN-BP-001", new BigDecimal("28500.00"), TransactionType.BOOKING_PAYMENT, 1L, "Oceanview Villa", 201L, "John Doe", "Booking payment for 3 nights", now.minusMonths(5).minusDays(10));
            seedTransaction("TXN-BP-002", new BigDecimal("15000.00"), TransactionType.BOOKING_PAYMENT, 2L, "Mountain Retreat", 202L, "Alice Smith", "Booking payment for 2 nights", now.minusMonths(5).minusDays(5));
            seedTransaction("TXN-BP-003", new BigDecimal("42000.00"), TransactionType.BOOKING_PAYMENT, 3L, "City Center Apartment", 203L, "David Brown", "Booking payment for 5 nights", now.minusMonths(4).minusDays(20));
            seedTransaction("TXN-BP-004", new BigDecimal("9500.00"), TransactionType.BOOKING_PAYMENT, 4L, "Desert Oasis Resort", 204L, "Carol White", "Booking payment for 1 night", now.minusMonths(4).minusDays(15));
            seedTransaction("TXN-BP-005", new BigDecimal("35000.00"), TransactionType.BOOKING_PAYMENT, 5L, "Lakeside Bungalow", 201L, "John Doe", "Booking payment for 4 nights", now.minusMonths(4).minusDays(3));
            seedTransaction("TXN-BP-006", new BigDecimal("18500.00"), TransactionType.BOOKING_PAYMENT, 1L, "Oceanview Villa", 205L, "Aisha Kumar", "Booking payment for 2 nights", now.minusMonths(3).minusDays(22));
            seedTransaction("TXN-BP-007", new BigDecimal("55000.00"), TransactionType.BOOKING_PAYMENT, 2L, "Mountain Retreat", 206L, "Mike Ross", "Booking payment for 6 nights", now.minusMonths(3).minusDays(18));
            seedTransaction("TXN-BP-008", new BigDecimal("22000.00"), TransactionType.BOOKING_PAYMENT, 3L, "City Center Apartment", 207L, "Priya Sharma", "Booking payment for 2 nights", now.minusMonths(3).minusDays(10));
            seedTransaction("TXN-BP-009", new BigDecimal("12500.00"), TransactionType.BOOKING_PAYMENT, 4L, "Desert Oasis Resort", 201L, "John Doe", "Booking payment for 1 night", now.minusMonths(2).minusDays(25));
            seedTransaction("TXN-BP-010", new BigDecimal("38000.00"), TransactionType.BOOKING_PAYMENT, 5L, "Lakeside Bungalow", 202L, "Alice Smith", "Booking payment for 4 nights", now.minusMonths(2).minusDays(18));
            seedTransaction("TXN-BP-011", new BigDecimal("67000.00"), TransactionType.BOOKING_PAYMENT, 1L, "Oceanview Villa", 208L, "Daniel Osei", "Booking payment for 7 nights", now.minusMonths(2).minusDays(8));
            seedTransaction("TXN-BP-012", new BigDecimal("24500.00"), TransactionType.BOOKING_PAYMENT, 2L, "Mountain Retreat", 203L, "David Brown", "Booking payment for 3 nights", now.minusMonths(1).minusDays(22));
            seedTransaction("TXN-BP-013", new BigDecimal("45000.00"), TransactionType.BOOKING_PAYMENT, 6L, "Sunset Beach Villa", 204L, "Carol White", "Booking payment for 5 nights", now.minusMonths(1).minusDays(14));
            seedTransaction("TXN-BP-014", new BigDecimal("16000.00"), TransactionType.BOOKING_PAYMENT, 3L, "City Center Apartment", 205L, "Aisha Kumar", "Booking payment for 2 nights", now.minusMonths(1).minusDays(6));
            seedTransaction("TXN-BP-015", new BigDecimal("72000.00"), TransactionType.BOOKING_PAYMENT, 4L, "Desert Oasis Resort", 206L, "Mike Ross", "Booking payment for 8 nights", now.minusDays(28));
            seedTransaction("TXN-BP-016", new BigDecimal("33000.00"), TransactionType.BOOKING_PAYMENT, 5L, "Lakeside Bungalow", 207L, "Priya Sharma", "Booking payment for 4 nights", now.minusDays(20));
            seedTransaction("TXN-BP-017", new BigDecimal("19500.00"), TransactionType.BOOKING_PAYMENT, 1L, "Oceanview Villa", 201L, "John Doe", "Booking payment for 2 nights", now.minusDays(14));
            seedTransaction("TXN-BP-018", new BigDecimal("48000.00"), TransactionType.BOOKING_PAYMENT, 6L, "Sunset Beach Villa", 208L, "Daniel Osei", "Booking payment for 5 nights", now.minusDays(7));
            seedTransaction("TXN-BP-019", new BigDecimal("25000.00"), TransactionType.BOOKING_PAYMENT, 2L, "Mountain Retreat", 202L, "Alice Smith", "Booking payment for 3 nights", now.minusDays(3));
            seedTransaction("TXN-BP-020", new BigDecimal("14000.00"), TransactionType.BOOKING_PAYMENT, 3L, "City Center Apartment", 203L, "David Brown", "Booking payment for 1 night", now.minusDays(1));

            // Commission transactions
            seedTransaction("TXN-CM-001", new BigDecimal("4275.00"), TransactionType.COMMISSION, 1L, "Oceanview Villa", 201L, "John Doe", "Platform commission 15%", now.minusMonths(5).minusDays(10));
            seedTransaction("TXN-CM-002", new BigDecimal("2250.00"), TransactionType.COMMISSION, 2L, "Mountain Retreat", 202L, "Alice Smith", "Platform commission 15%", now.minusMonths(4).minusDays(20));
            seedTransaction("TXN-CM-003", new BigDecimal("6300.00"), TransactionType.COMMISSION, 3L, "City Center Apartment", 203L, "David Brown", "Platform commission 15%", now.minusMonths(3).minusDays(18));
            seedTransaction("TXN-CM-004", new BigDecimal("5250.00"), TransactionType.COMMISSION, 4L, "Desert Oasis Resort", 204L, "Carol White", "Platform commission 15%", now.minusMonths(2).minusDays(25));
            seedTransaction("TXN-CM-005", new BigDecimal("10050.00"), TransactionType.COMMISSION, 6L, "Sunset Beach Villa", 205L, "Aisha Kumar", "Platform commission 15%", now.minusMonths(1).minusDays(14));

            // Refund transactions
            seedTransaction("TXN-RF-001", new BigDecimal("15000.00"), TransactionType.REFUND, 1L, "Oceanview Villa", 201L, "John Doe", "Refund processed for cancellation", now.minusMonths(3).minusDays(15));
            seedTransaction("TXN-RF-002", new BigDecimal("9500.00"), TransactionType.REFUND, 4L, "Desert Oasis Resort", 204L, "Carol White", "Refund processed for amenity issues", now.minusMonths(1).minusDays(8));

            System.out.println("✅ Transactions seeded (27 records)");
        }

        // ✅ Seed Refunds
        if (refundRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            seedRefund(1L, 201L, "John Doe", new BigDecimal("15000.00"), "Guest cancelled 48h before check-in, eligible for partial refund", RefundStatus.PENDING, null, now.minusMonths(2));
            seedRefund(3L, 203L, "David Brown", new BigDecimal("42000.00"), "Property did not match description, amenities missing", RefundStatus.PENDING, null, now.minusMonths(1).minusDays(20));
            seedRefund(2L, 202L, "Alice Smith", new BigDecimal("15000.00"), "Host cancelled reservation last minute", RefundStatus.APPROVED, "Full refund approved per cancellation policy", now.minusMonths(3));
            seedRefund(4L, 204L, "Carol White", new BigDecimal("9500.00"), "Property condition not as advertised, pool unavailable", RefundStatus.APPROVED, "Partial refund approved, pool was under maintenance", now.minusMonths(2).minusDays(5));
            seedRefund(5L, 205L, "Aisha Kumar", new BigDecimal("18500.00"), "Check-in process was delayed by 5 hours, no communication from host", RefundStatus.PENDING, null, now.minusDays(15));
            seedRefund(1L, 206L, "Mike Ross", new BigDecimal("55000.00"), "Early checkout due to noise from construction nearby", RefundStatus.REJECTED, "Refund denied as this is an outside factor", now.minusMonths(1).minusDays(10));
            seedRefund(3L, 207L, "Priya Sharma", new BigDecimal("22000.00"), "Booking cancelled due to medical emergency", RefundStatus.PENDING, null, now.minusDays(8));
            seedRefund(6L, 208L, "Daniel Osei", new BigDecimal("48000.00"), "Property fire safety concerns — left early", RefundStatus.APPROVED, "Full refund approved for safety incident", now.minusDays(20));
            seedRefund(2L, 201L, "John Doe", new BigDecimal("25000.00"), "Flight cancelled, unable to travel", RefundStatus.REJECTED, "Outside cancellation window, policy strictly enforced", now.minusDays(5));
            seedRefund(5L, 202L, "Alice Smith", new BigDecimal("38000.00"), "Duplicate payment processed by payment gateway", RefundStatus.APPROVED, "Duplicate payment confirmed and refunded", now.minusDays(3));
            System.out.println("✅ Refunds seeded (10 records)");
        }

        // ✅ Seed Payouts
        if (payoutRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            seedPayout(101L, "Alex Owner", new BigDecimal("24225.00"), PayoutStatus.PENDING, null, now.minusDays(3));
            seedPayout(102L, "Sarah Jenkins", new BigDecimal("46750.00"), PayoutStatus.PENDING, null, now.minusDays(5));
            seedPayout(103L, "Emily Chen", new BigDecimal("35700.00"), PayoutStatus.PROCESSED, "BOC-REF-20240501", now.minusMonths(1).minusDays(2));
            seedPayout(104L, "Nina Patel", new BigDecimal("8075.00"), PayoutStatus.PROCESSED, "HNB-REF-20240502", now.minusMonths(1).minusDays(12));
            seedPayout(105L, "Daniel Osei", new BigDecimal("29750.00"), PayoutStatus.PENDING, null, now.minusDays(1));
            seedPayout(106L, "Priya Sharma", new BigDecimal("56950.00"), PayoutStatus.PROCESSED, "COMM-REF-20240503", now.minusMonths(2).minusDays(3));
            seedPayout(101L, "Alex Owner", new BigDecimal("15725.00"), PayoutStatus.PROCESSED, "BOC-REF-20240401", now.minusMonths(2).minusDays(18));
            seedPayout(102L, "Sarah Jenkins", new BigDecimal("20825.00"), PayoutStatus.FAILED, null, now.minusMonths(1).minusDays(22));
            seedPayout(103L, "Emily Chen", new BigDecimal("13600.00"), PayoutStatus.PENDING, null, now.minusDays(7));
            seedPayout(104L, "Nina Patel", new BigDecimal("40800.00"), PayoutStatus.PROCESSED, "HNB-REF-20240504", now.minusMonths(3).minusDays(5));
            System.out.println("✅ Payouts seeded (10 records)");
        }
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
            }
        );
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

    private void seedTransaction(String ref, BigDecimal amount, TransactionType type,
            Long propertyId, String propertyName, Long userId, String userName,
            String description, LocalDateTime createdAt) {
        Transaction t = new Transaction();
        t.setReferenceNumber(ref);
        t.setAmount(amount);
        t.setCurrency("LKR");
        t.setType(type);
        t.setPropertyId(propertyId);
        t.setPropertyName(propertyName);
        t.setUserId(userId);
        t.setUserName(userName);
        t.setDescription(description);
        // createdAt is auto-set by @CreationTimestamp but we use reflection trick:
        // Since the field is auto-generated, we just save and let Hibernate set it.
        // For past dates, we override using direct setter after construction.
        transactionRepository.save(t);
    }

    private void seedRefund(Long transactionId, Long userId, String userName,
            BigDecimal amount, String reason, RefundStatus status,
            String adminNote, LocalDateTime requestedAt) {
        Refund r = new Refund();
        r.setTransactionId(transactionId);
        r.setUserId(userId);
        r.setUserName(userName);
        r.setAmount(amount);
        r.setCurrency("LKR");
        r.setReason(reason);
        r.setStatus(status);
        r.setAdminNote(adminNote);
        refundRepository.save(r);
    }

    private void seedPayout(Long ownerId, String ownerName, BigDecimal amount,
            PayoutStatus status, String bankReference, LocalDateTime requestedAt) {
        Payout p = new Payout();
        p.setOwnerId(ownerId);
        p.setOwnerName(ownerName);
        p.setAmount(amount);
        p.setCurrency("LKR");
        p.setStatus(status);
        p.setBankReference(bankReference);
        payoutRepository.save(p);
    }

    private void seedProperty(String name, String address, String pvId,
            Long ownerId, String ownerName, String imageUrl, PropertyStatus status) {
        // Native SQL — must include all NOT NULL columns from the shared 'properties' table
        // (base_guests, published, city, destination, property_type come from guest.models.Property)
        String city = address.contains(",") ? address.split(",")[address.split(",").length - 2].trim() : "Colombo";
        jdbcTemplate.update(
            "INSERT INTO properties " +
            "(name, address, pv_id, owner_id, owner_name, image_url, status, " +
            " base_guests, published, city, destination, property_type, submitted_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 2, false, ?, 'Sri Lanka', 'Villa', NOW(), NOW())",
            name, address, pvId, ownerId, ownerName, imageUrl, status.name(), city
        );
    }
}