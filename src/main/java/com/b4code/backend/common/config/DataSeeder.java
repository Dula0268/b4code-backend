package com.b4code.backend.common.config;

import com.b4code.backend.dao.*;
import com.b4code.backend.models.*;
import com.b4code.backend.models.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final DisputeRepository disputeRepository;
    private final FlaggedReviewRepository flaggedReviewRepository;
    private final ModerationHistoryRepository moderationHistoryRepository;
    private final PayoutRepository payoutRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final AuditLogRepository auditLogRepository;

    public DataSeeder(UserRepository userRepository,
                      PropertyRepository propertyRepository,
                      RoomRepository roomRepository,
                      AmenityRepository amenityRepository,
                      BookingRepository bookingRepository,
                      ReviewRepository reviewRepository,
                      DisputeRepository disputeRepository,
                      FlaggedReviewRepository flaggedReviewRepository,
                      ModerationHistoryRepository moderationHistoryRepository,
                      PayoutRepository payoutRepository,
                      PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate,
                      AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
        this.amenityRepository = amenityRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.disputeRepository = disputeRepository;
        this.flaggedReviewRepository = flaggedReviewRepository;
        this.moderationHistoryRepository = moderationHistoryRepository;
        this.payoutRepository = payoutRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void run(String... args) {
        seedCoreUsers();
        seedGuestData();
        seedAdminAndOwnerData();
    }
    
    private void seedCoreUsers() {
        userRepository.findByEmail("admin@primestay.com").ifPresentOrElse(
                admin -> {
                    if (admin.getRole() != UserRole.ADMIN) {
                        admin.setRole(UserRole.ADMIN);
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
                    admin.setRole(UserRole.ADMIN);
                    admin.setStatus(UserStatus.ACTIVE);
                    userRepository.save(admin);
                    System.out.println("✅ Admin user created");
                });

        seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", UserRole.GUEST, null, UserStatus.ACTIVE);
        seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", UserRole.OWNER, null, UserStatus.ACTIVE);
    }

    private void seedUserIfMissing(String email, String password, String first, String last, UserRole role, Long propertyId, UserStatus status) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    if (user.getRole() != role || (status != null && user.getStatus() != status)) {
                        user.setRole(role);
                        if (status != null) user.setStatus(status);
                        userRepository.save(user);
                    }
                },
                () -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setFirstName(first);
                    user.setLastName(last);
                    user.setRole(role);
                    user.setPropertyId(propertyId);
                    user.setStatus(status != null ? status : UserStatus.ACTIVE);
                    userRepository.save(user);
                });
    }

    private void seedGuestData() {
        log.info("🧹 Wiping old guest and owner data for a clean start...");
        jdbcTemplate.execute("TRUNCATE TABLE guest.bookings RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE guest.reviews RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE owner.properties RESTART IDENTITY CASCADE");

        log.info("🌱 Seeding 12 guest properties with 6 owners...");
        seedAllProperties();
        log.info("✅ Property seeding complete");
    }

    private void seedAllProperties() {
        // Ensure 6 owners exist
        List<User> owners = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            final int index = i;
            String email = "owner" + index + "@primestay.com";
            User owner = userRepository.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setPasswordHash(passwordEncoder.encode("owner123"));
                u.setFirstName("Owner");
                u.setLastName(String.valueOf(index));
                u.setRole(UserRole.OWNER);
                u.setStatus(UserStatus.ACTIVE);
                return userRepository.save(u);
            });
            owners.add(owner);
        }

        // We need a guest user for reviews
        User guestUser = userRepository.findByEmail("guest@primestay.com").orElseThrow();

        String[] titles = {
            "Colombo Sky Residency", "Galle Fort Heritage Cottage", "Kandy Lake View Manor",
            "Negombo Beachfront Boutique", "Arugambe Beach Camp", "Ella Mountain Eco Cabin",
            "Nuwara Eliya Tea Villa", "Mirissa Ocean Breeze", "Trincomalee Sunrise Retreat",
            "Sigiriya Forest Lodge", "Yala Safari Camp", "Bentota Riverside Resort"
        };
        String[] types = {"Apartment", "Guesthouse", "Villa", "Hotel", "Guesthouse", "Villa", "Villa", "Apartment", "Hotel", "Guesthouse", "Hotel", "Villa"};
        String[] cities = {"Colombo", "Galle", "Kandy", "Negombo", "Arugam Bay", "Ella", "Nuwara Eliya", "Mirissa", "Trincomalee", "Sigiriya", "Yala", "Bentota"};
        
        String[] propertyImages = {
            "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800", // Colombo
            "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800", // Galle
            "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800", // Kandy
            "https://images.unsplash.com/photo-1497366216548-37526070297c?w=800", // Negombo
            "https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800", // Arugam Bay
            "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800", // Ella
            "https://images.unsplash.com/photo-1502672260266-1c1c8742db5d?w=800", // Nuwara Eliya
            "https://images.unsplash.com/photo-1499955085172-a104c9463ece?w=800", // Mirissa
            "https://images.unsplash.com/photo-1549294413-26f195200c16?w=800", // Trincomalee
            "https://images.unsplash.com/photo-1518732714860-b62714ce0c59?w=800", // Sigiriya
            "https://images.unsplash.com/photo-1551882547-ff40c0d5b150?w=800", // Yala
            "https://images.unsplash.com/photo-1542314831-c6a4d27ce6a2?w=800"  // Bentota
        };

        String[] roomImages = {
            "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800", // Room 1
            "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800", // Room 2
            "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800", // Room 3
            "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800"  // Room 4
        };

        com.b4code.backend.models.RoomType[] roomTypes = {
            com.b4code.backend.models.RoomType.STANDARD_ROOM,
            com.b4code.backend.models.RoomType.DELUXE_ROOM,
            com.b4code.backend.models.RoomType.SUITE,
            com.b4code.backend.models.RoomType.FAMILY_ROOM
        };

        com.b4code.backend.models.BedType[] bedTypes = {
            com.b4code.backend.models.BedType.SINGLE,
            com.b4code.backend.models.BedType.DOUBLE,
            com.b4code.backend.models.BedType.QUEEN,
            com.b4code.backend.models.BedType.KING
        };

        String[] reviewComments = {
            "Absolutely wonderful experience, highly recommend!",
            "Great location but the service could be better.",
            "Very clean and comfortable, perfect for a family vacation.",
            "Breathtaking views and amazing food!"
        };

        for (int i = 0; i < 12; i++) {
            User owner = owners.get(i % 6); // 2 properties per owner

            Property p = Property.builder()
                    .name(titles[i])
                    .city(cities[i])
                    .country("Sri Lanka")
                    .addressLine1("Main Road, " + cities[i])
                    .latitude(6.9 + (i * 0.05))
                    .longitude(79.8 + (i * 0.05))
                    .ownerId(owner.getId())
                    .description("Enjoy a wonderful stay at " + titles[i] + ", offering premium amenities and breathtaking views.")
                    .freeCancellation(i % 2 == 0)
                    .breakfastIncluded(i % 3 == 0)
                    .petFriendly(i % 4 == 0)
                    .accessibility(i % 5 == 0)
                    .status(com.b4code.backend.models.enums.PropertyStatus.values()[i % 4]) // Mix of PENDING, APPROVED, etc.
                    .createdAt(LocalDateTime.now().minusDays(i * 3L + 1)) // Staggered creation dates
                    .build();
            p = propertyRepository.save(p);

            // 4 Amenities
            Set<Amenity> ams = new HashSet<>();
            ams.add(Amenity.builder().name("Free WiFi").property(p).build());
            ams.add(Amenity.builder().name("Air Conditioning").property(p).build());
            ams.add(Amenity.builder().name("Swimming Pool").property(p).build());
            ams.add(Amenity.builder().name("Spa and Wellness").property(p).build());
            p.setAmenities(ams);

            // Property and Room Images
            List<Image> images = new ArrayList<>();
            images.add(Image.builder().property(p).url(propertyImages[i]).type(com.b4code.backend.models.ImageType.PROPERTY).build());
            for (int j = 0; j < 4; j++) {
                images.add(Image.builder().property(p).url(roomImages[j]).type(com.b4code.backend.models.ImageType.ROOM).build());
            }
            p.setImages(images);
            p = propertyRepository.save(p);

            // 4 Rooms
            List<Room> createdRooms = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                Room r = Room.builder()
                    .property(p)
                    .roomType(roomTypes[j])
                    .maxOccupancy(2 + j)
                    .bedType(bedTypes[j])
                    .pricePerNight(new BigDecimal(10000 + (j * 5000)))
                    .image(p.getImages().get(j + 1)) // Index 0 is property image, 1-4 are room images
                    .build();
                createdRooms.add(roomRepository.save(r));
            }

            // 4 Reviews
            for (int r = 0; r < 4; r++) {
                // To attach a review, we need a booking for this guest. Since reviews are linked to bookings, 
                // we must create a dummy booking first for the review to reference.
                Booking dummyBooking = Booking.builder()
                    .room(createdRooms.get(r))
                    .property(p)
                    .checkIn(LocalDate.now().minusDays(10 + r))
                    .checkOut(LocalDate.now().minusDays(8 + r))
                    .adults(2)
                    .children(0)
                    .totalAmount(new BigDecimal(20000))
                    .taxAmount(new BigDecimal(2000))
                    .promoCode("")
                    .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                    .build();
                
                // Save booking
                dummyBooking = bookingRepository.save(dummyBooking);

                // Create review
                Review review = Review.builder()
                    .booking(dummyBooking)
                    .property(p)
                    .guest(guestUser)
                    .overallRating(5 - (r % 2))
                    .comment(reviewComments[r])
                    .photoUrls(roomImages[r])
                    .build();
                
                reviewRepository.save(review);
            }
        }
    }

    private void seedAdminAndOwnerData() {
        log.info("🌱 Seeding Admin and Owner specific data...");

        User admin = userRepository.findByEmail("admin@primestay.com").orElse(null);
        User guest = userRepository.findByEmail("guest@primestay.com").orElse(null);
        User owner = userRepository.findByEmail("owner1@primestay.com").orElse(null);

        List<Property> properties = propertyRepository.findAll();
        List<Review> reviews = reviewRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();

        if (admin == null || guest == null || owner == null || properties.isEmpty() || reviews.isEmpty() || bookings.isEmpty()) {
            log.warn("⚠️ Missing core entities, skipping Admin/Owner seeding.");
            return;
        }

        Property prop1 = properties.size() > 0 ? properties.get(0) : null;
        Property prop2 = properties.size() > 1 ? properties.get(1) : prop1;

        Booking booking1 = bookings.size() > 0 ? bookings.get(0) : null;
        Booking booking2 = bookings.size() > 1 ? bookings.get(1) : booking1;

        Review review1 = reviews.size() > 0 ? reviews.get(0) : null;
        Review review2 = reviews.size() > 1 ? reviews.get(1) : review1;

        if (disputeRepository.count() == 0) {
            Dispute d1 = new Dispute();
            d1.setDisputeId("DISP-" + java.util.UUID.randomUUID().toString());
            d1.setGuest(guest);
            d1.setProperty(prop1);
            d1.setBooking(booking1);
            d1.setReason("Property not as described");
            d1.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
            d1.setAmount(new BigDecimal("5000.00"));
            disputeRepository.save(d1);

            Dispute d2 = new Dispute();
            d2.setDisputeId("DISP-" + java.util.UUID.randomUUID().toString());
            d2.setGuest(guest);
            d2.setProperty(prop2);
            d2.setBooking(booking2);
            d2.setReason("Host was unresponsive");
            d2.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
            d2.setAmount(new BigDecimal("2500.00"));
            disputeRepository.save(d2);

            Dispute d3 = new Dispute();
            d3.setDisputeId("DISP-" + java.util.UUID.randomUUID().toString());
            d3.setGuest(guest);
            d3.setProperty(prop1);
            d3.setBooking(booking1);
            d3.setReason("Amenities missing");
            d3.setStatus(com.b4code.backend.models.enums.DisputeStatus.RESOLVED);
            d3.setAmount(new BigDecimal("1000.00"));
            disputeRepository.save(d3);

            Dispute d4 = new Dispute();
            d4.setDisputeId("DISP-" + java.util.UUID.randomUUID().toString());
            d4.setGuest(guest);
            d4.setProperty(prop2);
            d4.setBooking(booking2);
            d4.setReason("Unclean room");
            d4.setStatus(com.b4code.backend.models.enums.DisputeStatus.OPEN);
            d4.setAmount(new BigDecimal("1500.00"));
            disputeRepository.save(d4);

            log.info("✅ 4 Disputes seeded explicitly");
        }

        if (flaggedReviewRepository.count() == 0) {
            FlaggedReview f1 = new FlaggedReview();
            f1.setReview(review1);
            f1.setFlagReason("Harassment");
            f1.setStatus(com.b4code.backend.models.enums.ReviewStatus.FLAGGED);
            flaggedReviewRepository.save(f1);

            FlaggedReview f2 = new FlaggedReview();
            f2.setReview(review2);
            f2.setFlagReason("Spam / Scam");
            f2.setStatus(com.b4code.backend.models.enums.ReviewStatus.FLAGGED);
            flaggedReviewRepository.save(f2);

            FlaggedReview f3 = new FlaggedReview();
            f3.setReview(review1);
            f3.setFlagReason("Profanity");
            f3.setStatus(com.b4code.backend.models.enums.ReviewStatus.APPROVED);
            flaggedReviewRepository.save(f3);

            FlaggedReview f4 = new FlaggedReview();
            f4.setReview(review2);
            f4.setFlagReason("Policy Violation");
            f4.setStatus(com.b4code.backend.models.enums.ReviewStatus.REMOVED);
            flaggedReviewRepository.save(f4);

            log.info("✅ 4 FlaggedReviews seeded explicitly");
        }

        if (moderationHistoryRepository.count() == 0) {
            ModerationHistory m1 = new ModerationHistory();
            m1.setCaseId("MOD-" + java.util.UUID.randomUUID().toString());
            m1.setAdmin(admin);
            m1.setActionTaken(com.b4code.backend.models.enums.ModerationAction.REVIEW_REMOVED);
            m1.setOutcome("Removed due to severe harassment");
            m1.setResolvedAt(LocalDateTime.now());
            moderationHistoryRepository.save(m1);

            ModerationHistory m2 = new ModerationHistory();
            m2.setCaseId("MOD-" + java.util.UUID.randomUUID().toString());
            m2.setAdmin(admin);
            m2.setActionTaken(com.b4code.backend.models.enums.ModerationAction.REVIEW_KEPT);
            m2.setOutcome("Review kept, not a scam");
            m2.setResolvedAt(LocalDateTime.now().minusDays(1));
            moderationHistoryRepository.save(m2);

            ModerationHistory m3 = new ModerationHistory();
            m3.setCaseId("MOD-" + java.util.UUID.randomUUID().toString());
            m3.setAdmin(admin);
            m3.setActionTaken(com.b4code.backend.models.enums.ModerationAction.REFUND_ISSUED);
            m3.setOutcome("Refund issued for missing amenities");
            m3.setResolvedAt(LocalDateTime.now().minusDays(2));
            moderationHistoryRepository.save(m3);

            ModerationHistory m4 = new ModerationHistory();
            m4.setCaseId("MOD-" + java.util.UUID.randomUUID().toString());
            m4.setAdmin(admin);
            m4.setActionTaken(com.b4code.backend.models.enums.ModerationAction.APPEAL_DENIED);
            m4.setOutcome("Policy violation confirmed, appeal denied");
            m4.setResolvedAt(LocalDateTime.now().minusDays(3));
            moderationHistoryRepository.save(m4);

            log.info("✅ 4 ModerationHistories seeded explicitly");
        }

        if (payoutRepository.count() == 0) {
            Payout p1 = Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(prop1.getId())
                .propertyName(prop1.getName())
                .amount(new BigDecimal("45000.00"))
                .hotelAmount(new BigDecimal("40000.00"))
                .foodAmount(new BigDecimal("5000.00"))
                .commissionAmount(new BigDecimal("4500.00"))
                .commissionRate(new BigDecimal("10.00"))
                .currency("LKR")
                .status(PayoutStatus.PENDING)
                .bankReference("BANK001")
                .build();
            payoutRepository.save(p1);

            Payout p2 = Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(prop2.getId())
                .propertyName(prop2.getName())
                .amount(new BigDecimal("22500.00"))
                .hotelAmount(new BigDecimal("20000.00"))
                .foodAmount(new BigDecimal("2500.00"))
                .commissionAmount(new BigDecimal("2250.00"))
                .commissionRate(new BigDecimal("10.00"))
                .currency("LKR")
                .status(PayoutStatus.PROCESSED)
                .bankReference("BANK002")
                .build();
            payoutRepository.save(p2);

            Payout p3 = Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(prop1.getId())
                .propertyName(prop1.getName())
                .amount(new BigDecimal("18000.00"))
                .hotelAmount(new BigDecimal("15000.00"))
                .foodAmount(new BigDecimal("3000.00"))
                .commissionAmount(new BigDecimal("1800.00"))
                .commissionRate(new BigDecimal("10.00"))
                .currency("LKR")
                .status(PayoutStatus.FAILED)
                .bankReference("BANK003")
                .build();
            payoutRepository.save(p3);

            Payout p4 = Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(prop2.getId())
                .propertyName(prop2.getName())
                .amount(new BigDecimal("50000.00"))
                .hotelAmount(new BigDecimal("45000.00"))
                .foodAmount(new BigDecimal("5000.00"))
                .commissionAmount(new BigDecimal("5000.00"))
                .commissionRate(new BigDecimal("10.00"))
                .currency("LKR")
                .status(PayoutStatus.PENDING)
                .bankReference("BANK004")
                .build();
            payoutRepository.save(p4);

            Payout p5 = Payout.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFirstName() + " " + owner.getLastName())
                .propertyId(prop1.getId())
                .propertyName(prop1.getName())
                .amount(new BigDecimal("12000.00"))
                .hotelAmount(new BigDecimal("10000.00"))
                .foodAmount(new BigDecimal("2000.00"))
                .commissionAmount(new BigDecimal("1200.00"))
                .commissionRate(new BigDecimal("10.00"))
                .currency("LKR")
                .status(PayoutStatus.PROCESSED)
                .bankReference("BANK005")
                .build();
            payoutRepository.save(p5);

            log.info("✅ 5 Payouts seeded explicitly");
        }

        if (auditLogRepository.count() == 0) {
            AuditLog al1 = new AuditLog();
            al1.setUser(admin);
            al1.setAction("LOGIN_SUCCESS");
            al1.setEntity("AUTH");
            al1.setEntityDetail("admin@primestay.com");
            al1.setTimestamp(LocalDateTime.now().minusHours(2));
            auditLogRepository.save(al1);

            AuditLog al2 = new AuditLog();
            al2.setUser(owner);
            al2.setAction("LOGIN_SUCCESS");
            al2.setEntity("AUTH");
            al2.setEntityDetail("owner1@primestay.com");
            al2.setTimestamp(LocalDateTime.now().minusHours(5));
            auditLogRepository.save(al2);

            AuditLog al3 = new AuditLog();
            al3.setUser(admin);
            al3.setAction("PROPERTY_APPROVED");
            al3.setEntity("PROPERTY");
            al3.setEntityDetail("Property ID: " + prop1.getId());
            al3.setTimestamp(LocalDateTime.now().minusDays(1));
            auditLogRepository.save(al3);

            AuditLog al4 = new AuditLog();
            al4.setUser(admin);
            al4.setAction("CONFIG_CHANGE");
            al4.setEntity("SYSTEM");
            al4.setEntityDetail("Updated commission rate");
            al4.setTimestamp(LocalDateTime.now().minusDays(2));
            auditLogRepository.save(al4);

            log.info("✅ 4 Audit Logs seeded explicitly");
        }
    }
}
