package com.b4code.backend.common.config;

import com.b4code.backend.dao.*;
import com.b4code.backend.models.*;
import com.b4code.backend.models.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(2)
public class DataSeeder implements CommandLineRunner {

        private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

        private final UserRepository userRepository;
        private final FlaggedReviewRepository flaggedReviewRepository;
        private final DisputeRepository disputeRepository;
        private final MenuItemRepository menuItemRepository;
        private final PropertyRepository propertyRepository;
        private final RoomRepository roomRepository;
        private final AmenityRepository amenityRepository;
        private final BookingRepository bookingRepository;
        private final ReviewRepository reviewRepository;
        private final PromoCodeRepository promoCodeRepository;
        private final PlatformConfigRepository platformConfigRepository;
        private final PayoutRepository payoutRepository;
        private final RolePermissionRepository rolePermissionRepository;
        private final PasswordEncoder passwordEncoder;
        private final JdbcTemplate jdbcTemplate;

        private final Random random = new Random(42);
        private static final String IMG = "https://res.cloudinary.com/de0mj95bh/image/upload";

        private static final String[] GUEST_NAMES = {
                        "James Wilson", "Emma Thompson", "Liam Garcia", "Sophia Martinez",
                        "Noah Johnson", "Olivia Williams", "Ethan Brown", "Ava Davis",
                        "Mason Taylor", "Isabella Anderson", "Lucas Thomas", "Mia Jackson",
                        "Alexander White", "Charlotte Harris", "Benjamin Clark", "Amelia Lewis",
                        "Daniel Robinson", "Harper Walker", "Matthew Hall", "Evelyn Young"
        };

        private static final String[] POSITIVE_COMMENTS = {
                        "Absolutely stunning property! The views were breathtaking and the amenities were top-notch. Would definitely come back.",
                        "One of the best stays we've ever had. The host was incredibly attentive and the location was perfect.",
                        "Everything exceeded our expectations. The room was immaculate, the breakfast was delicious, and the staff went above and beyond.",
                        "A truly magical experience. Woke up to incredible views every morning. The attention to detail here is remarkable.",
                        "Perfect getaway from the city. The property is even more beautiful than the photos suggest. Highly recommended!",
                        "We had an amazing time. The pool area was gorgeous, the rooms were spacious, and the food was exceptional.",
                        "Such a peaceful and luxurious retreat. Every aspect of our stay was carefully thought out. Will be back for sure.",
                        "The hospitality here is world-class. From check-in to check-out, everything was seamless and enjoyable.",
                        "Incredible value for money. The property offers so much — great food, beautiful grounds, and wonderful service.",
                        "A hidden gem in Sri Lanka. The location is fantastic, the rooms are elegant, and the experience is unforgettable.",
                        "We celebrated our anniversary here and it was perfect. The romantic setting and excellent service made it truly special.",
                        "The architecture and design of this property are outstanding. Every corner is Instagram-worthy!",
                        "Best breakfast we've had in Sri Lanka. Fresh, local ingredients prepared with care. The room was comfortable and clean.",
                        "The staff remembered our names from day one. That personal touch made all the difference. Outstanding hospitality.",
                        "Loved the eco-friendly approach without compromising on luxury. The sustainability efforts here are impressive."
        };

        private static final String[] OWNER_REPLIES = {
                        "Thank you so much for your wonderful review! We're thrilled you enjoyed your stay. Looking forward to welcoming you back!",
                        "We appreciate your kind words! Our team works hard to ensure every guest has a memorable experience.",
                        "What a lovely review! Thank you for choosing us for your special occasion. We'd love to host you again.",
                        null, null, null
        };

        public DataSeeder(UserRepository userRepository,
                        FlaggedReviewRepository flaggedReviewRepository,
                        DisputeRepository disputeRepository,
                        MenuItemRepository menuItemRepository,
                        PropertyRepository propertyRepository,
                        RoomRepository roomRepository,
                        AmenityRepository amenityRepository,
                        BookingRepository bookingRepository,
                        ReviewRepository reviewRepository,
                        PromoCodeRepository promoCodeRepository,
                        PlatformConfigRepository platformConfigRepository,
                        PayoutRepository payoutRepository,
                        RolePermissionRepository rolePermissionRepository,
                        PasswordEncoder passwordEncoder,
                        JdbcTemplate jdbcTemplate) {
                this.userRepository = userRepository;
                this.flaggedReviewRepository = flaggedReviewRepository;
                this.disputeRepository = disputeRepository;
                this.menuItemRepository = menuItemRepository;
                this.propertyRepository = propertyRepository;
                this.roomRepository = roomRepository;
                this.amenityRepository = amenityRepository;
                this.bookingRepository = bookingRepository;
                this.reviewRepository = reviewRepository;
                this.promoCodeRepository = promoCodeRepository;
                this.platformConfigRepository = platformConfigRepository;
                this.payoutRepository = payoutRepository;
                this.rolePermissionRepository = rolePermissionRepository;
                this.passwordEncoder = passwordEncoder;
                this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public void run(String... args) {
                seedCoreProperties();
                seedCoreUsers();
                seedAdminUsers();
                seedFlaggedReviews();
                seedDisputes();
                seedMenuItems();
                seedGuestData();
                seedPlatformConfig();
                seedPayouts();
                seedRolePermissions();
        }

        private void seedRolePermissions() {
                long count = rolePermissionRepository.count();
                if (count > 0) {
                        log.info("✅ Role permissions already seeded ({} records)", count);
                        return;
                }

                List<RolePermission> permissions = new ArrayList<>();

                // ── Staff permissions ──────────────────────────────────────────
                permissions.add(buildPerm("Staff", "user", "order_management",
                                "Order Management", "Allow staff to view and manage guest food/room orders", true));
                permissions.add(buildPerm("Staff", "user", "menu_management",
                                "Menu Management", "Allow staff to create, edit, and manage property menus", true));
                permissions.add(buildPerm("Staff", "user", "qr_management",
                                "QR Management", "Allow staff to generate and manage QR codes for guest ordering", true));
                permissions.add(buildPerm("Staff", "user", "guest_messages",
                                "Guest Messages", "Allow staff to send and receive messages with guests", true));

                // ── Owner permissions ──────────────────────────────────────────
                permissions.add(buildPerm("Owner", "user", "manage_staff",
                                "Staff Management", "Allow owner to view and approve/reject pending staff registrations", true));
                permissions.add(buildPerm("Owner", "financial", "view_payouts",
                                "View Payouts", "Allow owner to access the payouts and earnings section", true));
                permissions.add(buildPerm("Owner", "user", "manage_listings",
                                "Manage Listings", "Allow owner to create, edit, and publish property listings", true));
                permissions.add(buildPerm("Owner", "user", "manage_availability",
                                "Manage Availability", "Allow owner to set room availability and rates", true));

                rolePermissionRepository.saveAll(permissions);
                log.info("✅ Role permissions seeded ({} records)", permissions.size());
        }

        private RolePermission buildPerm(String role, String section, String key, String label,
                        String description, boolean enabled) {
                RolePermission p = new RolePermission();
                p.setRoleName(role);
                p.setSection(section);
                p.setPermissionKey(key);
                p.setLabel(label);
                p.setDescription(description);
                p.setEnabled(enabled);
                return p;
        }

        private void seedCoreProperties() {
                if (propertyRepository.count() > 0) {
                        return;
                }

                Property p1 = new Property();
                p1.setName("Sunset Villa");
                p1.setPvId("PV-1001");
                p1.setOwnerName("Alex Owner");
                p1.setOwnerId(1L);
                p1.setStatus(PropertyStatus.APPROVED);
                p1.setSubmittedAt(LocalDateTime.now());
                p1.setAddress("123 Sunset Blvd, Miami, FL");
                propertyRepository.save(p1);

                Property p2 = new Property();
                p2.setName("Ocean Breeze");
                p2.setPvId("PV-1002");
                p2.setOwnerName("Alex Owner");
                p2.setOwnerId(1L);
                p2.setStatus(PropertyStatus.APPROVED);
                p2.setSubmittedAt(LocalDateTime.now());
                p2.setAddress("456 Ocean Dr, Miami, FL");
                propertyRepository.save(p2);

                System.out.println("✅ Test properties seeded");
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

                seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", UserRole.GUEST, null,
                                UserStatus.ACTIVE);
                seedUserIfMissing("guest1@primestay.com", "guest123", "Alice", "Guest", UserRole.GUEST, 1L,
                                UserStatus.ACTIVE);

                userRepository.findByEmail("guest1@primestay.com").ifPresent(u -> {
                        if (u.getPropertyId() == null || u.getPropertyId() != 1L) {
                                u.setPropertyId(1L);
                                userRepository.save(u);
                                System.out.println("✅ Updated guest1 propertyId to 1");
                        }
                });

                seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", UserRole.OWNER, null,
                                UserStatus.ACTIVE);

                seedUserIfMissing("staff@primestay.com", "staff123", "Mike", "Staff", UserRole.STAFF, 1L,
                                UserStatus.APPROVED);
                seedUserIfMissing("staff2@primestay.com", "staff123", "Jane", "Staff", UserRole.STAFF, 2L,
                                UserStatus.APPROVED);
        }

        private void seedAdminUsers() {
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

        private void seedFlaggedReviews() {
                if (flaggedReviewRepository.count() > 0) {
                        return;
                }

                seedFlaggedReview(101L, "Oceanview Villa", 201L, "Alice Smith", "AS", "blue",
                                "The place was a total mess and not as described. Bugs everywhere!", 1.5,
                                "Inappropriate Content",
                                ReviewStatus.FLAGGED);
                seedFlaggedReview(102L, "Mountain Retreat", 202L, "Bob Jones", "BJ", "green",
                                "Host demanded extra cash upon arrival. Very shady.", 2.0, "Policy Violation",
                                ReviewStatus.FLAGGED);
                seedFlaggedReview(103L, "City Center Apartment", 203L, "Carol White", "CW", "purple",
                                "Great place, but the neighbors were a bit loud.", 4.0, "Spam", ReviewStatus.FLAGGED);
                System.out.println("✅ Flagged reviews seeded");
        }

        private void seedDisputes() {
                if (disputeRepository.count() > 0) {
                        return;
                }

                seedDispute("DSP-1001", 201L, "Alice Smith", 101L, "Oceanview Villa", "BKG-9901",
                                "Host cancelled last minute, requesting full refund.", new BigDecimal("15000.00"),
                                "LKR",
                                "2026-06-01 to 2026-06-05", "Strict", 5, DisputeStatus.OPEN);
                seedDispute("DSP-1002", 204L, "David Brown", 104L, "Desert Oasis", "BKG-9902",
                                "Property amenities missing (no pool as advertised).", new BigDecimal("5000.00"), "LKR",
                                "2026-05-10 to 2026-05-12", "Moderate", 3, DisputeStatus.OPEN);
                System.out.println("✅ Disputes seeded");
        }

        private void seedMenuItems() {
                seedOrUpdateMenuItem(1L, "Classic Margherita Pizza", "Main",
                                "Fresh mozzarella, basil, and tomato sauce on a thin crust.", new BigDecimal("2500.00"),
                                List.of(
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485194/pro3e5jrllljbttvqsni.jpg",
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485195/v0tkfbvbokimxyjblsgc.jpg",
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485038/iknjlwvyxlusvpa6npex.jpg"));
                seedOrUpdateMenuItem(1L, "Sri Lankan Rice & Curry", "Main",
                                "Authentic village-style rice and curry with chicken and assorted vegetables.",
                                new BigDecimal("1800.00"),
                                List.of(
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485192/tsjra56wpkcjjsralkdt.jpg",
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485194/t6e27scjzdoufdwqfvga.jpg"));
                seedOrUpdateMenuItem(1L, "Watalappam", "Dessert",
                                "Traditional Sri Lankan coconut custard pudding with jaggery.",
                                new BigDecimal("850.00"),
                                List.of("https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485193/quifhrtj1wg0mjgb5pya.jpg"));
                seedOrUpdateMenuItem(1L, "Fresh King Coconut", "Drink", "Chilled natural king coconut water.",
                                new BigDecimal("450.00"),
                                List.of(
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485190/fjoolp2br10pqp56u2t3.jpg",
                                                "https://res.cloudinary.com/dfydjkjw8/image/upload/v1778485191/lk3whcfcoanysx611dnu.jpg"));
                System.out.println("✅ Menu items synced for Property 1");
        }

        private void seedGuestData() {
                long publishedCount = propertyRepository.countByPublishedTrue();
                log.info("📊 Current published properties: {}", publishedCount);

                boolean idMismatch = propertyRepository.findAll().stream()
                                .filter(p -> "Colombo Sky Residency".equals(p.getName()))
                                .findFirst()
                                .map(p -> p.getId() != 1)
                                .orElse(publishedCount > 0);

                if (publishedCount == 12 && !idMismatch) {
                        log.info("✅ Guest data already correctly seeded (12 properties)");
                        return;
                }

                log.info("🧹 Wiping old guest data for a clean start (Count: {}, ID Mismatch: {})...", publishedCount,
                                idMismatch);
                wipeGuestData();

                log.info("🌱 Seeding 12 guest properties with Cloudinary images...");
                seedAllProperties();
                log.info("✅ Property seeding complete");

                log.info("🌱 Seeding reviews and bookings...");
                seedReviews();
                log.info("✅ Review seeding complete");

                log.info("🌱 Seeding promo codes...");
                seedPromoCodes();
                log.info("✅ Promo code seeding complete");
        }

        private void wipeGuestData() {
                jdbcTemplate.execute(
                                "TRUNCATE TABLE guest.reviews, guest.bookings, owner.rooms, guest.promo_codes, owner.properties, guest.messages, guest.message_templates RESTART IDENTITY CASCADE");
        }

        private void seedUserIfMissing(String email, String password, String first, String last, UserRole role,
                        Long propertyId, UserStatus status) {
                userRepository.findByEmail(email).ifPresentOrElse(
                                user -> {
                                        if (user.getRole() != role
                                                        || (propertyId != null
                                                                        && !propertyId.equals(user.getPropertyId()))
                                                        || (status != null && user.getStatus() != status)) {
                                                user.setRole(role);
                                                user.setPropertyId(propertyId);
                                                if (status != null)
                                                        user.setStatus(status);
                                                userRepository.save(user);
                                                System.out.println("✅ Updated " + email + " to " + role + " role");
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
                                        System.out.println("✅ Seeded user: " + email);
                                });
        }

        private void seedAdminUser(String first, String last, String email, UserRole role, UserStatus status) {
                userRepository.findByEmail(email).ifPresentOrElse(
                                user -> {
                                        if (user.getRole() != role || (status != null && user.getStatus() != status)) {
                                                user.setRole(role);
                                                if (status != null)
                                                        user.setStatus(status);
                                                userRepository.save(user);
                                                System.out.println("✅ Updated admin user " + email + " to " + role
                                                                + " role");
                                        }
                                },
                                () -> {
                                        User u = new User();
                                        u.setFirstName(first);
                                        u.setLastName(last);
                                        u.setEmail(email);
                                        u.setPasswordHash(passwordEncoder.encode("password123"));
                                        u.setRole(role);
                                        u.setStatus(status);
                                        userRepository.save(u);
                                        System.out.println("✅ Seeded admin user: " + email);
                                });
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
                        BigDecimal price, List<String> imageUrls) {
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

        private Room seedRoom(Property property, String name, Integer capacity, BigDecimal price) {
                Room room = new Room();
                room.setProperty(property);
                room.setName(name);
                room.setRoomType(name);
                room.setMaxOccupancy(capacity);
                room.setPricePerNight(price);
                room.setAvailable(true);
                roomRepository.save(room);
                return room;
        }

        private void seedPlatformConfig() {
                if (platformConfigRepository.findByConfigKey("COMMISSION_RATE").isEmpty()) {
                        PlatformConfig config = PlatformConfig.builder()
                                        .configKey("COMMISSION_RATE")
                                        .configValue("20.00")
                                        .description("Platform commission rate applied to hotel booking revenue (%)")
                                        .build();
                        platformConfigRepository.save(config);
                        log.info("✅ PlatformConfig 'COMMISSION_RATE' seeded to 20%");
                }
        }

        private void seedPayouts() {
                if (payoutRepository.count() > 0) {
                        return;
                }

                // Payout 1 (Pending)
                Payout p1 = Payout.builder()
                                .ownerId(2L)
                                .ownerName("Alex Owner")
                                .propertyId(1L)
                                .propertyName("Sunset Villa")
                                .hotelAmount(new BigDecimal("150000.00"))
                                .foodAmount(new BigDecimal("25000.00"))
                                .commissionRate(new BigDecimal("20.00")) // standard
                                .commissionAmount(new BigDecimal("30000.00")) // 150000 * 20%
                                .amount(new BigDecimal("145000.00")) // 150000 - 30000 + 25000
                                .currency("LKR")
                                .status(PayoutStatus.PENDING)
                                .build();

                // Payout 2 (Pending)
                Payout p2 = Payout.builder()
                                .ownerId(3L)
                                .ownerName("Sarah Jenkins")
                                .propertyId(2L)
                                .propertyName("Ocean Breeze")
                                .hotelAmount(new BigDecimal("80000.00"))
                                .foodAmount(new BigDecimal("0.00"))
                                .commissionRate(new BigDecimal("20.00")) // standard
                                .commissionAmount(new BigDecimal("16000.00")) // 80000 * 20%
                                .amount(new BigDecimal("64000.00")) // 80000 - 16000 + 0
                                .currency("LKR")
                                .status(PayoutStatus.PENDING)
                                .build();

                // Payout 3 (Processed)
                Payout p3 = Payout.builder()
                                .ownerId(2L)
                                .ownerName("Alex Owner")
                                .propertyId(1L)
                                .propertyName("Sunset Villa")
                                .hotelAmount(new BigDecimal("200000.00"))
                                .foodAmount(new BigDecimal("50000.00"))
                                .commissionRate(new BigDecimal("15.00")) // custom override example
                                .commissionAmount(new BigDecimal("30000.00")) // 200000 * 15%
                                .amount(new BigDecimal("220000.00")) // 200000 - 30000 + 50000
                                .currency("LKR")
                                .status(PayoutStatus.PROCESSED)
                                .bankReference("TXN-987654321")
                                .processedAt(LocalDateTime.now().minusDays(2))
                                .build();

                // Payout 4 (Pending)
                Payout p4 = Payout.builder()
                                .ownerId(4L)
                                .ownerName("Emily Chen")
                                .propertyId(3L)
                                .propertyName("Colombo Sky Residency")
                                .hotelAmount(new BigDecimal("350000.00"))
                                .foodAmount(new BigDecimal("80000.00"))
                                .commissionRate(new BigDecimal("20.00"))
                                .commissionAmount(new BigDecimal("70000.00")) // 350000 * 20%
                                .amount(new BigDecimal("360000.00")) // 350000 - 70000 + 80000
                                .currency("LKR")
                                .status(PayoutStatus.PENDING)
                                .build();

                // Payout 5 (Pending)
                Payout p5 = Payout.builder()
                                .ownerId(5L)
                                .ownerName("Nina Patel")
                                .propertyId(4L)
                                .propertyName("Mountain Retreat")
                                .hotelAmount(new BigDecimal("95000.00"))
                                .foodAmount(new BigDecimal("12000.00"))
                                .commissionRate(new BigDecimal("20.00"))
                                .commissionAmount(new BigDecimal("19000.00")) // 95000 * 20%
                                .amount(new BigDecimal("88000.00")) // 95000 - 19000 + 12000
                                .currency("LKR")
                                .status(PayoutStatus.PENDING)
                                .build();

                payoutRepository.saveAll(List.of(p1, p2, p3, p4, p5));
                log.info("✅ Seeded 5 sample payouts with hotel/food breakdown");
        }

        // ============ MERGED FROM GuestDataSeeder ============

        private void seedAllProperties() {
                seedProperty1();
                seedProperty2();
                seedProperty3();
                seedProperty4();
                seedProperty5();
                seedProperty6();
                seedProperty7();
                seedProperty8();
                seedProperty9();
                seedProperty10();
                seedProperty11();
                seedProperty12();
        }

        private void seedProperty1() {
                Property p = Property.builder()
                                .pvId("PROP001").ownerId(1L).name("Colombo Sky Residency").city("Colombo 3")
                                .destination("Colombo 3")
                                .address("32 Galle Road, Colombo 03, Sri Lanka")
                                .propertyType("Apartment").badge("Superhost").latitude(6.9088).longitude(79.8543)
                                .imageSrc(IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
                                .description(
                                                "Experience Colombo's vibrant city life from this sleek sky-high apartment. Floor-to-ceiling windows frame panoramic views of the Indian Ocean and the city skyline. Designed with minimalist luxury in mind.")
                                .hostName("Priya Fernando").hostBio("Superhost · 5 years experience").hostYears(5)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("5000")).averageRating(4.92)
                                .reviewCount(148)
                                .published(true)
                                .amenities(
                                                "Wifi:Free High Speed WiFi,Wind:Air Conditioning,Waves:Rooftop Pool,Dumbbell:Fitness Center,Car:Valet Parking,Utensils:In-suite Kitchen,ShieldCheck:24hr Security,Coffee:Nespresso Machine")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Deluxe Ocean View Room").roomType("DOUBLE")
                                                .maxOccupancy(2).sqft(480)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("25000"))
                                                .originalPrice(new BigDecimal("28000")).tag("Refundable")
                                                .features("Private Balcony,Nespresso Machine")
                                                .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Panoramic Grand Suite").roomType("SUITE")
                                                .maxOccupancy(4).sqft(650)
                                                .bedType("2 Queen Beds").pricePerNight(new BigDecimal("35000"))
                                                .tag("Popular")
                                                .features("Floor-to-ceiling windows,Separate living area")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Sky Penthouse").roomType("SUITE").maxOccupancy(2)
                                                .sqft(900)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("55000"))
                                                .features("Private terrace,Marble bathroom,Butler service")
                                                .imageSrc(IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg")
                                                .available(true).build()));
        }

        private void seedProperty2() {
                Property p = Property.builder()
                                .pvId("PROP002").ownerId(1L).name("Galle Fort Heritage Cottage").city("Galle Fort")
                                .destination("Galle Fort").address("14 Church St, Galle Fort, Sri Lanka")
                                .propertyType("Guesthouse").latitude(6.0328).longitude(80.2170)
                                .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
                                .description(
                                                "Step back in time in this beautifully restored Dutch colonial cottage within the UNESCO-listed Galle Fort. Original 18th-century architecture blends seamlessly with modern comfort.")
                                .hostName("Marcus de Silva").hostBio("Heritage host · 7 years experience").hostYears(7)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("3000")).averageRating(4.95).reviewCount(92)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Coffee:Nescafe Bar,Wind:Ceiling Fans,Utensils:Guest Kitchen,ShieldCheck:Night Security")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Fort Master Room").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(380)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("35000"))
                                                .originalPrice(new BigDecimal("42000")).tag("Popular")
                                                .features("Fort wall views,Writing desk,Antique furnishings")
                                                .imageSrc(IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Heritage Suite").roomType("SUITE").maxOccupancy(4)
                                                .sqft(550)
                                                .bedType("2 Single Beds").pricePerNight(new BigDecimal("55000"))
                                                .tag("Refundable")
                                                .features("Shared living space,Stone archways")
                                                .imageSrc(IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg")
                                                .available(true).build()));
        }

        private void seedProperty3() {
                Property p = Property.builder()
                                .pvId("PROP003").ownerId(1L).name("Kandy Lake View Manor").city("Kandy")
                                .destination("Kandy")
                                .address("Lake Road, Kandy, Sri Lanka")
                                .propertyType("Villa").badge("Guest favorite").latitude(7.2906).longitude(80.6328)
                                .imageSrc(IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
                                .description(
                                                "A stately Colonial-style residence overlooking Kandy's sacred lake. Wake to the sound of temple bells and the sight of misty mountains. An absolute gem for culture and nature lovers.")
                                .hostName("Deepa Wijewardene").hostBio("Superhost · 4 years experience").hostYears(4)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("4500")).averageRating(4.90).reviewCount(76)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Wind:Air Conditioning,Waves:Infinity Pool,Utensils:Colonial Dining,Coffee:Tea Service,ShieldCheck:24hr Security")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Lake View Deluxe").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(520)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("42000"))
                                                .originalPrice(new BigDecimal("50000")).tag("Popular")
                                                .features("Lake views,Balcony,Marble bath")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Colonial Grand Suite").roomType("SUITE")
                                                .maxOccupancy(4).sqft(800)
                                                .bedType("2 Queen Beds").pricePerNight(new BigDecimal("65000"))
                                                .features("Lake and mountain views,Separate lounge,Clawfoot tub")
                                                .imageSrc(IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg")
                                                .available(true).build()));
        }

        private void seedProperty4() {
                Property p = Property.builder()
                                .pvId("PROP004").ownerId(1L).name("Negombo Beachfront Boutique").city("Negombo")
                                .destination("Negombo")
                                .address("Beach Road, Negombo, Sri Lanka")
                                .propertyType("Hotel").latitude(7.2119).longitude(79.8621)
                                .imageSrc(IMG + "/v1778126088/properties/zq3v7h8jl9kp5xy2wbcd.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
                                .description(
                                                "Wake up to the sound of waves at this stunning beachside boutique hotel on Negombo's golden coast — just 10 minutes from the international airport.")
                                .hostName("Ravi Gunawardena").hostBio("Superhost · 8 years experience").hostYears(8)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("7000")).averageRating(4.98)
                                .reviewCount(211)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:Beachfront Pool,Wind:Air Conditioning,Utensils:Seafood Restaurant,Bike:Bicycle Rental,ShieldCheck:24hr Security,Dumbbell:Gym,Car:Airport Shuttle")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Beachfront Deluxe Room").roomType("DOUBLE")
                                                .maxOccupancy(2).sqft(450)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("95000"))
                                                .originalPrice(new BigDecimal("115000")).tag("Last rooms")
                                                .features("Direct beach access,Outdoor shower")
                                                .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Garden Pool Villa").roomType("SUITE").maxOccupancy(4)
                                                .sqft(750)
                                                .bedType("2 Queen Beds").pricePerNight(new BigDecimal("150000"))
                                                .tag("Popular")
                                                .features("Private plunge pool,Outdoor dining")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build()));
        }

        private void seedProperty5() {
                Property p = Property.builder()
                                .pvId("PROP005").ownerId(1L).name("Arugambe Beach Camp").city("Arugam Bay")
                                .destination("Arugam Bay")
                                .address("Beach Road, Arugam Bay, Sri Lanka")
                                .propertyType("Guesthouse").latitude(7.1722).longitude(81.8396)
                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
                                .description(
                                                "A relaxed beachfront surf lodge on Arugam Bay's legendary waves. Perfect for surfers and beach bums seeking authentic Sri Lankan coastal life.")
                                .hostName("Thilak Bandara").hostBio("Beach host · 3 years experience").hostYears(3)
                                .hostSuperhost(false)
                                .baseGuests(2).extraGuestFee(new BigDecimal("3500")).averageRating(4.75).reviewCount(58)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:Beach Bar,Wind:Ceiling Fans,Utensils:Cafe,Bike:Surfboard Rental,ShieldCheck:Night Security")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Ocean Bungalow").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(300)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("18000"))
                                                .originalPrice(new BigDecimal("22000")).tag("Refundable")
                                                .features("Beach access,Outdoor shower,Hammock")
                                                .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Sunrise Suite").roomType("SUITE").maxOccupancy(3)
                                                .sqft(400)
                                                .bedType("1 King + 1 Single").pricePerNight(new BigDecimal("28000"))
                                                .features("Sea view balcony,Shared living area")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build()));
        }

        private void seedProperty6() {
                Property p = Property.builder()
                                .pvId("PROP006").ownerId(1L).name("Ella Mountain Eco Cabin").city("Ella")
                                .destination("Ella")
                                .address("Ella Gap Road, Ella, Sri Lanka")
                                .propertyType("Villa").latitude(6.8728).longitude(81.0466)
                                .imageSrc(IMG + "/v1778126091/properties/t6xfaton6b1icebnvhvy.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
                                .description(
                                                "Nestled within a working tea estate high above the famous Ella Gap, this sustainably-built eco cabin offers complete immersion in Sri Lanka's central highlands.")
                                .hostName("Nimal Rathnayake").hostBio("Eco host · 4 years experience").hostYears(4)
                                .hostSuperhost(false)
                                .baseGuests(2).extraGuestFee(new BigDecimal("3000")).averageRating(4.88)
                                .reviewCount(134)
                                .published(true)
                                .amenities(
                                                "Wifi:Solar WiFi,Leaf:Eco/Solar Power,Coffee:Tea Plantation Tour,Utensils:Farm-to-table Meals,Bike:Hiking Trails,ShieldCheck:Night Security")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.save(
                                Room.builder().property(p).name("Tea Estate Cabin").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(280).bedType("1 Queen Bed").pricePerNight(new BigDecimal("45000"))
                                                .originalPrice(new BigDecimal("55000")).tag("Refundable")
                                                .features("Mountain-view deck,Outdoor shower,Hammock")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build());
        }

        private void seedProperty7() {
                Property p = Property.builder()
                                .pvId("PROP007").ownerId(1L).name("Mirissa Oceanfront Villa").city("Mirissa")
                                .destination("Mirissa")
                                .address("Mirissa Bay Road, Mirissa, Sri Lanka")
                                .propertyType("Villa").badge("Guest favorite").latitude(5.9488).longitude(80.4593)
                                .imageSrc(IMG + "/v1778126092/properties/y8vz4z62rf4gtva5bwr4.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
                                .description(
                                                "Sri Lanka's most iconic oceanfront villa — positioned directly on Mirissa's crescent bay with uninterrupted 180° views of the Indian Ocean.")
                                .hostName("Saman Wickramasinghe").hostBio("Superhost · 6 years experience").hostYears(6)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("10000")).averageRating(4.96)
                                .reviewCount(88)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:Ocean-edge Infinity Pool,Wind:Air Conditioning,Utensils:Private Chef,Car:Airport Transfer,ShieldCheck:24hr Security,Dumbbell:Yoga Deck,Coffee:Butler on call")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Ocean Master Suite").roomType("SUITE").maxOccupancy(2)
                                                .sqft(650)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("120000"))
                                                .originalPrice(new BigDecimal("145000")).tag("Popular")
                                                .features("Sea-facing terrace,Rain shower,Soaking tub")
                                                .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Coral Bay Villa").roomType("SUITE").maxOccupancy(6)
                                                .sqft(1200)
                                                .bedType("3 King Beds").pricePerNight(new BigDecimal("250000"))
                                                .features("Entire lower villa,Private beach access,Chef included")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build()));
        }

        private void seedProperty8() {
                Property p = Property.builder()
                                .pvId("PROP008").ownerId(1L).name("Galle Dutch Period Mansion").city("Galle Fort")
                                .destination("Galle Fort").address("22 Leyn Baan Street, Galle Fort, Sri Lanka")
                                .propertyType("Villa").badge("Superhost").latitude(6.0309).longitude(80.2157)
                                .imageSrc(IMG + "/v1778126094/properties/ymd4nsi362k4sgmavggv.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
                                .description(
                                                "A masterwork of 18th-century Dutch colonial architecture — fully restored with museum-quality antiques and original Portuguese tile floors.")
                                .hostName("Anjalee Perera").hostBio("Superhost · 9 years experience").hostYears(9)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("12000")).averageRating(4.91)
                                .reviewCount(45)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:Courtyard Pool,Wind:Air Conditioning,Utensils:Heritage Restaurant,BookOpen:Antique Library,ShieldCheck:24hr Security,Coffee:Butler Service,Car:Chauffeur")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Dutch Governor's Suite").roomType("SUITE")
                                                .maxOccupancy(2).sqft(800)
                                                .bedType("1 King Canopy Bed").pricePerNight(new BigDecimal("180000"))
                                                .originalPrice(new BigDecimal("210000")).tag("Last rooms")
                                                .features("Courtyard views,Original tile floors,Clawfoot bathtub")
                                                .imageSrc(IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Rampart View Room").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(420)
                                                .bedType("1 Queen Bed").pricePerNight(new BigDecimal("120000"))
                                                .tag("Refundable")
                                                .features("Fort wall views,Antique writing desk")
                                                .imageSrc(IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg")
                                                .available(true).build()));
        }

        private void seedProperty9() {
                Property p = Property.builder()
                                .pvId("PROP009").ownerId(1L).name("Nuwara Eliya Tea Planter's Bungalow")
                                .city("Nuwara Eliya")
                                .destination("Nuwara Eliya").address("St Andrew's Drive, Nuwara Eliya, Sri Lanka")
                                .propertyType("Guesthouse").latitude(6.9497).longitude(80.7891)
                                .imageSrc(IMG + "/v1778126095/properties/gxjlvgg2qvwrdomrgjsm.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
                                .description(
                                                "A colonial-era tea planter's bungalow set amid 400 acres of pristine tea gardens at 6,000 feet. Log fires, tartan armchairs, and silver tea service await.")
                                .hostName("Victor Steuart").hostBio("Heritage host · 11 years experience").hostYears(11)
                                .hostSuperhost(false)
                                .baseGuests(2).extraGuestFee(new BigDecimal("5000")).averageRating(4.82)
                                .reviewCount(109)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Coffee:Silver Tea Service,Wind:Fireplace,Utensils:Colonial Dining,Bike:Estate Walks,ShieldCheck:Night Security")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Planter's Master Suite").roomType("SUITE")
                                                .maxOccupancy(2).sqft(550)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("65000"))
                                                .originalPrice(new BigDecimal("78000")).tag("Popular")
                                                .features("Fireplace,Mountain views,Claw-foot tub")
                                                .imageSrc(IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Tea Garden Room").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(350)
                                                .bedType("1 Queen Bed").pricePerNight(new BigDecimal("48000"))
                                                .tag("Refundable")
                                                .features("Garden views,Writing desk")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build()));
        }

        private void seedProperty10() {
                Property p = Property.builder()
                                .pvId("PROP010").ownerId(1L).name("Trincomalee Bay Resort").city("Trincomalee")
                                .destination("Trincomalee").address("Uppuveli Beach Road, Trincomalee, Sri Lanka")
                                .propertyType("Hotel").badge("Guest favorite").latitude(8.5874).longitude(81.2152)
                                .imageSrc(IMG + "/v1778126096/properties/xikgq4kkphkmd0tcuapr.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
                                .description(
                                                "A tropical beachfront resort on Trincomalee's turquoise Uppuveli Beach. Diving, snorkeling, and whale watching at your doorstep.")
                                .hostName("Kasun Rajapaksa").hostBio("Resort host · 5 years experience").hostYears(5)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("6000")).averageRating(4.89)
                                .reviewCount(156)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:Beachfront Pool,Wind:Air Conditioning,Utensils:Open-air Restaurant,Dumbbell:Dive Center,ShieldCheck:24hr Security,Car:Airport Transfer,Coffee:Beach Bar")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Beach Bungalow").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(400)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("72000"))
                                                .originalPrice(new BigDecimal("85000")).tag("Popular")
                                                .features("Direct beach access,Hammock,Outdoor shower")
                                                .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Ocean Suite").roomType("SUITE").maxOccupancy(4)
                                                .sqft(680)
                                                .bedType("2 Queen Beds").pricePerNight(new BigDecimal("110000"))
                                                .features("Panoramic ocean views,Private balcony,Jacuzzi")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build()));
        }

        private void seedProperty11() {
                Property p = Property.builder()
                                .pvId("PROP011").ownerId(1L).name("Sigiriya Jungle Lodge").city("Sigiriya")
                                .destination("Sigiriya")
                                .address("Sigiriya Road, Dambulla, Sri Lanka")
                                .propertyType("Villa").latitude(7.9570).longitude(80.7603)
                                .imageSrc(IMG + "/v1778126097/properties/seuqb344gkadlzmvs2mq.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg",
                                                IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
                                .description(
                                                "A luxury jungle lodge at the base of the iconic Sigiriya Rock Fortress. Wake to the calls of exotic birds and elephants passing in the distance.")
                                .hostName("Lakmal Bandara").hostBio("Nature host · 3 years experience").hostYears(3)
                                .hostSuperhost(false)
                                .baseGuests(2).extraGuestFee(new BigDecimal("4500")).averageRating(4.78).reviewCount(87)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:Plunge Pool,Utensils:Bush Dining,Bike:Safari Tours,ShieldCheck:Night Security,Coffee:Nature Bar")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.save(Room.builder().property(p).name("Jungle Pavilion").roomType("SUITE").maxOccupancy(2)
                                .sqft(500).bedType("1 King Bed").pricePerNight(new BigDecimal("58000"))
                                .originalPrice(new BigDecimal("68000")).tag("Refundable")
                                .features("Open-air bathroom,Rock fortress views,Mosquito net canopy")
                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true)
                                .build());
        }

        private void seedProperty12() {
                Property p = Property.builder()
                                .pvId("PROP012").ownerId(1L).name("Bentota River House").city("Bentota")
                                .destination("Bentota")
                                .address("River Avenue, Bentota, Sri Lanka")
                                .propertyType("Guesthouse").badge("Superhost").latitude(6.4271).longitude(79.9977)
                                .imageSrc(IMG + "/v1778126098/properties/fhgr2dqufndugvpimexq.jpg")
                                .galleryImages(String.join(",",
                                                IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg",
                                                IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg",
                                                IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg",
                                                IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
                                .description(
                                                "A charming river house where the Bentota River meets the Indian Ocean. Kayak, fish, and birdwatch from your private jetty.")
                                .hostName("Malini Jayasuriya").hostBio("Superhost · 6 years experience").hostYears(6)
                                .hostSuperhost(true)
                                .baseGuests(2).extraGuestFee(new BigDecimal("4000")).averageRating(4.94).reviewCount(72)
                                .published(true)
                                .amenities(
                                                "Wifi:Free WiFi,Waves:River Pool,Wind:Air Conditioning,Utensils:Home Cooking,Bike:Kayak Rental,ShieldCheck:Night Security,Car:Beach Shuttle")
                                .build();
                p = propertyRepository.save(p);
                roomRepository.saveAll(List.of(
                                Room.builder().property(p).name("Riverfront Suite").roomType("SUITE").maxOccupancy(2)
                                                .sqft(420)
                                                .bedType("1 King Bed").pricePerNight(new BigDecimal("42000"))
                                                .originalPrice(new BigDecimal("50000")).tag("Popular")
                                                .features("River views,Private balcony,Writing desk")
                                                .imageSrc(IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg")
                                                .available(true).build(),
                                Room.builder().property(p).name("Garden Room").roomType("DOUBLE").maxOccupancy(2)
                                                .sqft(300)
                                                .bedType("1 Queen Bed").pricePerNight(new BigDecimal("32000"))
                                                .tag("Refundable")
                                                .features("Garden access,Hammock")
                                                .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
                                                .available(true).build()));
        }

        private void seedReviews() {
                List<Property> properties = propertyRepository.findAll().stream()
                                .filter(Property::getPublished)
                                .toList();

                if (properties.isEmpty()) {
                        log.warn("⚠️ No published properties found — skipping review seeder");
                        return;
                }

                log.info("🌱 Seeding reviews for {} properties...", properties.size());

                for (Property property : properties) {
                        List<Room> rooms = roomRepository.findByPropertyId(property.getId());
                        if (rooms.isEmpty())
                                continue;

                        int reviewCount = 3 + random.nextInt(4);
                        double totalRating = 0;

                        for (int i = 0; i < reviewCount; i++) {
                                Room room = rooms.get(random.nextInt(rooms.size()));
                                String guestName = GUEST_NAMES[random.nextInt(GUEST_NAMES.length)];
                                String guestEmail = guestName.toLowerCase().replace(" ", ".") + "@email.com";

                                LocalDate checkIn = LocalDate.now().minusDays(30 + random.nextInt(180));
                                LocalDate checkOut = checkIn.plusDays(2 + random.nextInt(5));

                                Booking booking = Booking.builder()
                                                .room(room)
                                                .guestName(guestName)
                                                .guestEmail(guestEmail)
                                                .guestPhone("+94" + (700000000 + random.nextInt(99999999)))
                                                .checkIn(checkIn)
                                                .checkOut(checkOut)
                                                .guestCount(1 + random.nextInt(room.getMaxOccupancy()))
                                                .totalAmount(room.getPricePerNight()
                                                                .multiply(BigDecimal.valueOf(checkOut.toEpochDay()
                                                                                - checkIn.toEpochDay())))
                                                .taxAmount(BigDecimal.valueOf(2500))
                                                .discountAmount(BigDecimal.ZERO)
                                                .status(Booking.BookingStatus.COMPLETED)
                                                .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                                                .confirmationNumber("CONF-" + System.currentTimeMillis() + "-" + i)
                                                .build();
                                booking = bookingRepository.save(booking);

                                int overall = 4 + random.nextInt(2);
                                totalRating += overall;

                                String ownerReply = OWNER_REPLIES[random.nextInt(OWNER_REPLIES.length)];

                                Review review = Review.builder()
                                                .booking(booking)
                                                .property(property)
                                                .guestName(guestName)
                                                .overallRating(overall)
                                                .cleanlinessRating(3 + random.nextInt(3))
                                                .accuracyRating(3 + random.nextInt(3))
                                                .communicationRating(4 + random.nextInt(2))
                                                .locationRating(4 + random.nextInt(2))
                                                .valueRating(3 + random.nextInt(3))
                                                .comment(POSITIVE_COMMENTS[random.nextInt(POSITIVE_COMMENTS.length)])
                                                .isVerifiedStay(true)
                                                .ownerResponse(ownerReply)
                                                .build();

                                review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(120)));
                                if (ownerReply != null) {
                                        review.setOwnerRespondedAt(
                                                        review.getCreatedAt().plusDays(1 + random.nextInt(3)));
                                }

                                reviewRepository.save(review);
                        }

                        double avgRating = Math.round((totalRating / reviewCount) * 100.0) / 100.0;
                        property.setAverageRating(avgRating);
                        property.setReviewCount(reviewCount);
                        propertyRepository.save(property);
                }
        }

        private void seedPromoCodes() {
                List<PromoCode> promoCodes = new ArrayList<>();
                promoCodes.add(PromoCode.builder()
                                .code("ALLPROPS10")
                                .description("Platform-wide promo — 10% off any property")
                                .discountPercent(new BigDecimal("10.00"))
                                .validFrom(LocalDate.now().minusDays(30))
                                .validTo(LocalDate.now().plusMonths(12))
                                .maxUses(1000)
                                .currentUses(0)
                                .active(true)
                                .build());

                promoCodes.add(PromoCode.builder()
                                .code("PRIMESTAY15")
                                .description("Exclusive Prime Stay loyalty discount — 15% off any property")
                                .discountPercent(new BigDecimal("15.00"))
                                .validFrom(LocalDate.now().minusDays(30))
                                .validTo(LocalDate.now().plusMonths(18))
                                .maxUses(null)
                                .currentUses(0)
                                .active(true)
                                .build());

                promoCodeRepository.saveAll(promoCodes);

                List<Property> properties = propertyRepository.findAll().stream()
                                .filter(Property::getPublished)
                                .sorted(java.util.Comparator.comparing(Property::getId))
                                .toList();

                for (Property property : properties) {
                        String slug = property.getName()
                                        .toUpperCase()
                                        .replaceAll("[^A-Z0-9]+", "-")
                                        .replaceAll("^-+|-+$", "");

                        promoCodeRepository.save(PromoCode.builder()
                                        .code(slug + "-SPECIAL")
                                        .description("Special property promo for " + property.getName())
                                        .discountPercent(new BigDecimal("20.00"))
                                        .validFrom(LocalDate.now().minusDays(30))
                                        .validTo(LocalDate.now().plusMonths(12))
                                        .maxUses(100)
                                        .currentUses(0)
                                        .active(true)
                                        .propertyId(property.getId())
                                        .build());
                }
        }
}
