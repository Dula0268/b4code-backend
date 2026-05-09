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
import com.b4code.backend.modules.admin.enums.PropertyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.b4code.backend.modules.guest.dao.PropertyRepository;
import com.b4code.backend.modules.guest.dao.RoomRepository;
import com.b4code.backend.modules.guest.dao.BookingRepository;
import com.b4code.backend.modules.guest.dao.ReviewRepository;
import com.b4code.backend.modules.guest.dao.PromoCodeRepository;
import com.b4code.backend.modules.guest.models.Property;
import com.b4code.backend.modules.guest.models.Room;
import com.b4code.backend.modules.guest.models.Booking;
import com.b4code.backend.modules.guest.models.Review;
import com.b4code.backend.modules.guest.models.PromoCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final com.b4code.backend.modules.admin.dao.PropertyRepository adminPropertyRepository;
    private final FlaggedReviewRepository flaggedReviewRepository;
    private final DisputeRepository disputeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PromoCodeRepository promoCodeRepository;
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

    // Manual constructor to avoid Lombok @RequiredArgsConstructor issues
        public DataSeeder(UserRepository userRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            com.b4code.backend.modules.admin.dao.PropertyRepository adminPropertyRepository,
            @Qualifier("guestPropertyRepository") PropertyRepository propertyRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            PromoCodeRepository promoCodeRepository,
            FlaggedReviewRepository flaggedReviewRepository,
            DisputeRepository disputeRepository) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.adminPropertyRepository = adminPropertyRepository;
        this.flaggedReviewRepository = flaggedReviewRepository;
        this.disputeRepository = disputeRepository;
        this.passwordEncoder = passwordEncoder;
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.promoCodeRepository = promoCodeRepository;
    }


    public void run(String... args) {

        // 1. Seed Properties first so we have IDs to link to
        if (propertyRepository.count() == 0) {
            com.b4code.backend.modules.admin.models.Property p1 = new com.b4code.backend.modules.admin.models.Property();
            p1.setName("Sunset Villa");
            p1.setPvId("PV-1001");
            p1.setOwnerName("Alex Owner");
            p1.setOwnerId(1L);
            p1.setStatus(PropertyStatus.APPROVED);
            p1.setSubmittedAt(LocalDateTime.now());
            p1.setAddress("123 Sunset Blvd, Miami, FL");
            adminPropertyRepository.save(p1);

            com.b4code.backend.modules.admin.models.Property p2 = new com.b4code.backend.modules.admin.models.Property();
            p2.setName("Ocean Breeze");
            p2.setPvId("PV-1002");
            p2.setOwnerName("Alex Owner");
            p2.setOwnerId(1L);
            p2.setStatus(PropertyStatus.APPROVED);
            p2.setSubmittedAt(LocalDateTime.now());
            p2.setAddress("456 Ocean Dr, Miami, FL");
            adminPropertyRepository.save(p2);

            System.out.println("✅ Test properties seeded");
        }

        // 2. Ensure admin always exists
        userRepository.findByEmail("admin@primestay.com").ifPresentOrElse(
                admin -> {
                    if (admin.getRole() != User.Role.ADMIN) {
                        admin.setRole(User.Role.ADMIN);
                        userRepository.save(admin);
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
        seedUserIfMissing("guest@primestay.com", "guest123", "John", "Doe", User.Role.GUEST, null, User.UserStatus.ACTIVE);
        seedUserIfMissing("owner@primestay.com", "owner123", "Alex", "Owner", User.Role.OWNER, null, User.UserStatus.ACTIVE);
        
        // ✅ Specific Staff Login (Linked to Property 1 and APPROVED)
        seedUserIfMissing("staff@primestay.com", "staff123", "Mike", "Staff", User.Role.STAFF, 1L, User.UserStatus.APPROVED);

        // 4. Admin users table
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

        if (propertyRepository.countByPublishedTrue() == 0) {
            log.info("🌱 Seeding properties...");
            seedAllProperties();
            log.info("✅ Property seeding complete");
        }

        if (reviewRepository.count() == 0) {
            log.info("🌱 Seeding reviews...");
            seedReviews();
            log.info("✅ Review seeding complete");
        }

        if (promoCodeRepository.count() == 0) {
            log.info("🌱 Seeding promo codes...");
            seedPromoCodes();
            log.info("✅ Promo code seeding complete");
        }
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
            if (rooms.isEmpty()) continue;

            // Seed 3-6 reviews per property
            int reviewCount = 3 + random.nextInt(4);
            double totalRating = 0;

            for (int i = 0; i < reviewCount; i++) {
                Room room = rooms.get(random.nextInt(rooms.size()));
                String guestName = GUEST_NAMES[random.nextInt(GUEST_NAMES.length)];
                String guestEmail = guestName.toLowerCase().replace(" ", ".") + "@email.com";

                // Create a completed booking first (review requires a booking)
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
                        .totalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(checkOut.toEpochDay() - checkIn.toEpochDay())))
                        .taxAmount(BigDecimal.valueOf(2500))
                        .discountAmount(BigDecimal.ZERO)
                        .status(Booking.BookingStatus.COMPLETED)
                        .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                        .confirmationNumber("CONF-" + System.currentTimeMillis() + "-" + i)
                        .build();
                booking = bookingRepository.save(booking);

                // Create review
                int overall = 4 + random.nextInt(2); // 4 or 5
                totalRating += overall;

                String ownerReply = OWNER_REPLIES[random.nextInt(OWNER_REPLIES.length)];

                Review review = Review.builder()
                        .booking(booking)
                        .property(property)
                        .guestName(guestName)
                        .overallRating(overall)
                        .cleanlinessRating(3 + random.nextInt(3)) // 3-5
                        .accuracyRating(3 + random.nextInt(3))
                        .communicationRating(4 + random.nextInt(2)) // 4-5
                        .locationRating(4 + random.nextInt(2))
                        .valueRating(3 + random.nextInt(3))
                        .comment(POSITIVE_COMMENTS[random.nextInt(POSITIVE_COMMENTS.length)])
                        .isVerifiedStay(true)
                        .ownerResponse(ownerReply)
                        .build();

                // Set createdAt to a past date
                review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(120)));
                if (ownerReply != null) {
                    review.setOwnerRespondedAt(review.getCreatedAt().plusDays(1 + random.nextInt(3)));
                }

                reviewRepository.save(review);
            }

            // Update property average rating and review count
            double avgRating = Math.round((totalRating / reviewCount) * 100.0) / 100.0;
            property.setAverageRating(avgRating);
            property.setReviewCount(reviewCount);
            propertyRepository.save(property);
        }
    }


    private void seedUserIfMissing(String email, String password, String first, String last, User.Role role, Long propertyId, User.UserStatus status) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFirstName(first);
            user.setLastName(last);
            user.setRole(role);
            user.setStatus(status);
            userRepository.save(user);
        }


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

    private void seedFlaggedReview(Long propertyId, String propertyName, Long guestId, String guestName,
                                   String guestInitial, String guestAvatarColor, String reviewText,
                                   Double rating, String flagReason, ReviewStatus status) {
        FlaggedReview flaggedReview = new FlaggedReview();
        flaggedReview.setPropertyId(propertyId);
        flaggedReview.setPropertyName(propertyName);
        flaggedReview.setGuestId(guestId);
        flaggedReview.setGuestName(guestName);
        flaggedReview.setGuestInitial(guestInitial);
        flaggedReview.setGuestAvatarColor(guestAvatarColor);
        flaggedReview.setReviewText(reviewText);
        flaggedReview.setRating(rating);
        flaggedReview.setFlagReason(flagReason);
        flaggedReview.setStatus(status);
        flaggedReviewRepository.save(flaggedReview);
    }

    private void seedDispute(String disputeId, Long guestId, String guestName, Long propertyId, String propertyName,
                             String bookingId, String reason, BigDecimal amount, String currency, String stayDates,
                             String cancellationPolicy, Integer daysUntilAutoClose, DisputeStatus status) {
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
        disputeRepository.save(dispute);
    }

private void seedAllProperties() {
        seedProperty1(); seedProperty2(); seedProperty3(); seedProperty4();
        seedProperty5(); seedProperty6(); seedProperty7(); seedProperty8();
        seedProperty9(); seedProperty10(); seedProperty11(); seedProperty12();
    }

    private void seedProperty1() {
        Property p = Property.builder()
            .name("Colombo Sky Residency").city("Colombo 3").destination("Colombo 3").address("32 Galle Road, Colombo 03, Sri Lanka")
            .propertyType("Apartment").badge("Superhost").latitude(6.9088).longitude(79.8543)
            .imageSrc(IMG + "/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
            .description("Experience Colombo's vibrant city life from this sleek sky-high apartment. Floor-to-ceiling windows frame panoramic views of the Indian Ocean and the city skyline. Designed with minimalist luxury in mind.")
            .hostName("Priya Fernando").hostBio("Superhost · 5 years experience").hostYears(5).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("5000")).averageRating(4.92).reviewCount(148).published(true)
            .amenities("Wifi:Free High Speed WiFi,Wind:Air Conditioning,Waves:Rooftop Pool,Dumbbell:Fitness Center,Car:Valet Parking,Utensils:In-suite Kitchen,ShieldCheck:24hr Security,Coffee:Nespresso Machine")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Deluxe Ocean View Room").roomType("DOUBLE").maxOccupancy(2).sqft(480).bedType("1 King Bed").pricePerNight(new BigDecimal("25000")).originalPrice(new BigDecimal("28000")).tag("Refundable").features("Private Balcony,Nespresso Machine").imageSrc(IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg").available(true).build(),
            Room.builder().property(p).name("Panoramic Grand Suite").roomType("SUITE").maxOccupancy(4).sqft(650).bedType("2 Queen Beds").pricePerNight(new BigDecimal("35000")).tag("Popular").features("Floor-to-ceiling windows,Separate living area").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build(),
            Room.builder().property(p).name("Sky Penthouse").roomType("SUITE").maxOccupancy(2).sqft(900).bedType("1 King Bed").pricePerNight(new BigDecimal("55000")).features("Private terrace,Marble bathroom,Butler service").imageSrc(IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg").available(true).build()
        ));
    }

    private void seedProperty2() {
        Property p = Property.builder()
            .name("Galle Fort Heritage Cottage").city("Galle Fort").destination("Galle Fort").address("14 Church St, Galle Fort, Sri Lanka")
            .propertyType("Guesthouse").latitude(6.0328).longitude(80.2170)
            .imageSrc(IMG + "/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
            .description("Step back in time in this beautifully restored Dutch colonial cottage within the UNESCO-listed Galle Fort. Original 18th-century architecture blends seamlessly with modern comfort.")
            .hostName("Chamari De Silva").hostBio("Superhost · 7 years experience").hostYears(7).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("4000")).averageRating(4.85).reviewCount(92).published(true)
            .amenities("Wifi:Free WiFi,Wind:Air Conditioning,BookOpen:Library,Coffee:Garden Café,ShieldCheck:24hr Security,Bike:Bicycle Rental")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Heritage Garden Room").roomType("DOUBLE").maxOccupancy(2).sqft(320).bedType("1 King Bed").pricePerNight(new BigDecimal("35000")).originalPrice(new BigDecimal("42000")).tag("Refundable").features("Courtyard access,Antique furnishings").imageSrc(IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg").available(true).build(),
            Room.builder().property(p).name("Fort View Loft").roomType("DOUBLE").maxOccupancy(2).sqft(280).bedType("1 Queen Bed").pricePerNight(new BigDecimal("40000")).tag("Popular").features("Fort wall views,Skylight bathroom").imageSrc(IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg").available(true).build()
        ));
    }

    private void seedProperty3() {
        Property p = Property.builder()
            .name("Kandy Hilltop Luxury Villa").city("Kandy").destination("Kandy").address("7 Rajapihilla Mawatha, Kandy, Sri Lanka")
            .propertyType("Villa").badge("Guest favorite").latitude(7.2906).longitude(80.6337)
            .imageSrc(IMG + "/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
            .description("Perched above the misty hills overlooking the sacred Kandy Lake and the Temple of the Tooth, this private luxury villa offers an unparalleled blend of nature and elegance.")
            .hostName("Roshan Mendis").hostBio("Superhost · 6 years experience").hostYears(6).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("8000")).averageRating(5.0).reviewCount(67).published(true)
            .amenities("Wifi:Free High Speed WiFi,Waves:Infinity Pool,Wind:Air Conditioning,Utensils:Private Chef,Car:Chauffeur Service,Dumbbell:Yoga Pavilion,ShieldCheck:24hr Security,Coffee:Butler Service")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Master Lake View Suite").roomType("SUITE").maxOccupancy(2).sqft(700).bedType("1 King Bed").pricePerNight(new BigDecimal("75000")).originalPrice(new BigDecimal("90000")).tag("Popular").features("Lake-facing balcony,Rain shower,Bathtub").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build(),
            Room.builder().property(p).name("Garden Pavilion Suite").roomType("SUITE").maxOccupancy(4).sqft(950).bedType("2 King Beds").pricePerNight(new BigDecimal("120000")).features("Private garden,Outdoor soaking tub,Kitchenette").imageSrc(IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg").available(true).build()
        ));
    }

    private void seedProperty4() {
        Property p = Property.builder()
            .name("Colombo Boutique Business Suite").city("Colombo 7").destination("Colombo 7").address("18 Ward Place, Colombo 07, Sri Lanka")
            .propertyType("Apartment").latitude(6.9060).longitude(79.8605)
            .imageSrc(IMG + "/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg"))
            .description("A refined urban retreat in the prestigious Colombo 7 enclave, designed for the modern business traveller.")
            .hostName("Dilrukshi Jayawardena").hostBio("Superhost · 4 years experience").hostYears(4).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("6000")).averageRating(4.75).reviewCount(53).published(true)
            .amenities("Wifi:Gigabit WiFi,Wind:Air Conditioning,Monitor:Work Desk & Monitor,Coffee:Nespresso Machine,Car:Parking,ShieldCheck:24hr Security")
            .build();
        p = propertyRepository.save(p);
        roomRepository.save(Room.builder().property(p).name("Executive Business Suite").roomType("SUITE").maxOccupancy(2).sqft(520).bedType("1 King Bed").pricePerNight(new BigDecimal("85000")).tag("Refundable").features("Dual monitor setup,Standing desk,Meeting table for 4").imageSrc(IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg").available(true).build());
    }

    private void seedProperty5() {
        Property p = Property.builder()
            .name("Negombo Beachside Retreat").city("Negombo").destination("Negombo").address("78 Lewis Place, Negombo, Sri Lanka")
            .propertyType("Hotel").badge("Superhost").latitude(7.2083).longitude(79.8358)
            .imageSrc(IMG + "/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
            .description("Wake up to the sound of waves at this stunning beachside boutique hotel on Negombo's golden coast — just 10 minutes from the international airport.")
            .hostName("Ravi Gunawardena").hostBio("Superhost · 8 years experience").hostYears(8).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("7000")).averageRating(4.98).reviewCount(211).published(true)
            .amenities("Wifi:Free WiFi,Waves:Beachfront Pool,Wind:Air Conditioning,Utensils:Seafood Restaurant,Bike:Bicycle Rental,ShieldCheck:24hr Security,Dumbbell:Gym,Car:Airport Shuttle")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Beachfront Deluxe Room").roomType("DOUBLE").maxOccupancy(2).sqft(450).bedType("1 King Bed").pricePerNight(new BigDecimal("95000")).originalPrice(new BigDecimal("115000")).tag("Last rooms").features("Direct beach access,Outdoor shower").imageSrc(IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg").available(true).build(),
            Room.builder().property(p).name("Garden Pool Villa").roomType("SUITE").maxOccupancy(4).sqft(750).bedType("2 Queen Beds").pricePerNight(new BigDecimal("150000")).tag("Popular").features("Private plunge pool,Outdoor dining").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build()
        ));
    }

    private void seedProperty6() {
        Property p = Property.builder()
            .name("Ella Mountain Eco Cabin").city("Ella").destination("Ella").address("Ella Gap Road, Ella, Sri Lanka")
            .propertyType("Villa").latitude(6.8728).longitude(81.0466)
            .imageSrc(IMG + "/v1778126091/properties/t6xfaton6b1icebnvhvy.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
            .description("Nestled within a working tea estate high above the famous Ella Gap, this sustainably-built eco cabin offers complete immersion in Sri Lanka's central highlands.")
            .hostName("Nimal Rathnayake").hostBio("Eco host · 4 years experience").hostYears(4).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("3000")).averageRating(4.88).reviewCount(134).published(true)
            .amenities("Wifi:Solar WiFi,Leaf:Eco/Solar Power,Coffee:Tea Plantation Tour,Utensils:Farm-to-table Meals,Bike:Hiking Trails,ShieldCheck:Night Security")
            .build();
        p = propertyRepository.save(p);
        roomRepository.save(Room.builder().property(p).name("Tea Estate Cabin").roomType("DOUBLE").maxOccupancy(2).sqft(280).bedType("1 Queen Bed").pricePerNight(new BigDecimal("45000")).originalPrice(new BigDecimal("55000")).tag("Refundable").features("Mountain-view deck,Outdoor shower,Hammock").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build());
    }

    private void seedProperty7() {
        Property p = Property.builder()
            .name("Mirissa Oceanfront Villa").city("Mirissa").destination("Mirissa").address("Mirissa Bay Road, Mirissa, Sri Lanka")
            .propertyType("Villa").badge("Guest favorite").latitude(5.9488).longitude(80.4593)
            .imageSrc(IMG + "/v1778126092/properties/y8vz4z62rf4gtva5bwr4.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
            .description("Sri Lanka's most iconic oceanfront villa — positioned directly on Mirissa's crescent bay with uninterrupted 180° views of the Indian Ocean.")
            .hostName("Saman Wickramasinghe").hostBio("Superhost · 6 years experience").hostYears(6).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("10000")).averageRating(4.96).reviewCount(88).published(true)
            .amenities("Wifi:Free WiFi,Waves:Ocean-edge Infinity Pool,Wind:Air Conditioning,Utensils:Private Chef,Car:Airport Transfer,ShieldCheck:24hr Security,Dumbbell:Yoga Deck,Coffee:Butler on call")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Ocean Master Suite").roomType("SUITE").maxOccupancy(2).sqft(650).bedType("1 King Bed").pricePerNight(new BigDecimal("120000")).originalPrice(new BigDecimal("145000")).tag("Popular").features("Sea-facing terrace,Rain shower,Soaking tub").imageSrc(IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg").available(true).build(),
            Room.builder().property(p).name("Coral Bay Villa").roomType("SUITE").maxOccupancy(6).sqft(1200).bedType("3 King Beds").pricePerNight(new BigDecimal("250000")).features("Entire lower villa,Private beach access,Chef included").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build()
        ));
    }

    private void seedProperty8() {
        Property p = Property.builder()
            .name("Galle Dutch Period Mansion").city("Galle Fort").destination("Galle Fort").address("22 Leyn Baan Street, Galle Fort, Sri Lanka")
            .propertyType("Villa").badge("Superhost").latitude(6.0309).longitude(80.2157)
            .imageSrc(IMG + "/v1778126094/properties/ymd4nsi362k4sgmavggv.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
            .description("A masterwork of 18th-century Dutch colonial architecture — fully restored with museum-quality antiques and original Portuguese tile floors.")
            .hostName("Anjalee Perera").hostBio("Superhost · 9 years experience").hostYears(9).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("12000")).averageRating(4.91).reviewCount(45).published(true)
            .amenities("Wifi:Free WiFi,Waves:Courtyard Pool,Wind:Air Conditioning,Utensils:Heritage Restaurant,BookOpen:Antique Library,ShieldCheck:24hr Security,Coffee:Butler Service,Car:Chauffeur")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Dutch Governor's Suite").roomType("SUITE").maxOccupancy(2).sqft(800).bedType("1 King Canopy Bed").pricePerNight(new BigDecimal("180000")).originalPrice(new BigDecimal("210000")).tag("Last rooms").features("Courtyard views,Original tile floors,Clawfoot bathtub").imageSrc(IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg").available(true).build(),
            Room.builder().property(p).name("Rampart View Room").roomType("DOUBLE").maxOccupancy(2).sqft(420).bedType("1 Queen Bed").pricePerNight(new BigDecimal("120000")).tag("Refundable").features("Fort wall views,Antique writing desk").imageSrc(IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg").available(true).build()
        ));
    }

    private void seedProperty9() {
        Property p = Property.builder()
            .name("Nuwara Eliya Tea Planter's Bungalow").city("Nuwara Eliya").destination("Nuwara Eliya").address("St Andrew's Drive, Nuwara Eliya, Sri Lanka")
            .propertyType("Guesthouse").latitude(6.9497).longitude(80.7891)
            .imageSrc(IMG + "/v1778126095/properties/gxjlvgg2qvwrdomrgjsm.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
            .description("A colonial-era tea planter's bungalow set amid 400 acres of pristine tea gardens at 6,000 feet. Log fires, tartan armchairs, and silver tea service await.")
            .hostName("Victor Steuart").hostBio("Heritage host · 11 years experience").hostYears(11).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("5000")).averageRating(4.82).reviewCount(109).published(true)
            .amenities("Wifi:Free WiFi,Coffee:Silver Tea Service,Wind:Fireplace,Utensils:Colonial Dining,Bike:Estate Walks,ShieldCheck:Night Security")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Planter's Master Suite").roomType("SUITE").maxOccupancy(2).sqft(550).bedType("1 King Bed").pricePerNight(new BigDecimal("65000")).originalPrice(new BigDecimal("78000")).tag("Popular").features("Fireplace,Mountain views,Claw-foot tub").imageSrc(IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg").available(true).build(),
            Room.builder().property(p).name("Tea Garden Room").roomType("DOUBLE").maxOccupancy(2).sqft(350).bedType("1 Queen Bed").pricePerNight(new BigDecimal("48000")).tag("Refundable").features("Garden views,Writing desk").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build()
        ));
    }

    private void seedProperty10() {
        Property p = Property.builder()
            .name("Trincomalee Bay Resort").city("Trincomalee").destination("Trincomalee").address("Uppuveli Beach Road, Trincomalee, Sri Lanka")
            .propertyType("Hotel").badge("Guest favorite").latitude(8.5874).longitude(81.2152)
            .imageSrc(IMG + "/v1778126096/properties/xikgq4kkphkmd0tcuapr.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
            .description("A tropical beachfront resort on Trincomalee's turquoise Uppuveli Beach. Diving, snorkeling, and whale watching at your doorstep.")
            .hostName("Kasun Rajapaksa").hostBio("Resort host · 5 years experience").hostYears(5).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("6000")).averageRating(4.89).reviewCount(156).published(true)
            .amenities("Wifi:Free WiFi,Waves:Beachfront Pool,Wind:Air Conditioning,Utensils:Open-air Restaurant,Dumbbell:Dive Center,ShieldCheck:24hr Security,Car:Airport Transfer,Coffee:Beach Bar")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Beach Bungalow").roomType("DOUBLE").maxOccupancy(2).sqft(400).bedType("1 King Bed").pricePerNight(new BigDecimal("72000")).originalPrice(new BigDecimal("85000")).tag("Popular").features("Direct beach access,Hammock,Outdoor shower").imageSrc(IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg").available(true).build(),
            Room.builder().property(p).name("Ocean Suite").roomType("SUITE").maxOccupancy(4).sqft(680).bedType("2 Queen Beds").pricePerNight(new BigDecimal("110000")).features("Panoramic ocean views,Private balcony,Jacuzzi").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build()
        ));
    }

    private void seedProperty11() {
        Property p = Property.builder()
            .name("Sigiriya Jungle Lodge").city("Sigiriya").destination("Sigiriya").address("Sigiriya Road, Dambulla, Sri Lanka")
            .propertyType("Villa").latitude(7.9570).longitude(80.7603)
            .imageSrc(IMG + "/v1778126097/properties/seuqb344gkadlzmvs2mq.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg", IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg"))
            .description("A luxury jungle lodge at the base of the iconic Sigiriya Rock Fortress. Wake to the calls of exotic birds and elephants passing in the distance.")
            .hostName("Lakmal Bandara").hostBio("Nature host · 3 years experience").hostYears(3).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("4500")).averageRating(4.78).reviewCount(87).published(true)
            .amenities("Wifi:Free WiFi,Waves:Plunge Pool,Utensils:Bush Dining,Bike:Safari Tours,ShieldCheck:Night Security,Coffee:Nature Bar")
            .build();
        p = propertyRepository.save(p);
        roomRepository.save(Room.builder().property(p).name("Jungle Pavilion").roomType("SUITE").maxOccupancy(2).sqft(500).bedType("1 King Bed").pricePerNight(new BigDecimal("58000")).originalPrice(new BigDecimal("68000")).tag("Refundable").features("Open-air bathroom,Rock fortress views,Mosquito net canopy").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build());
    }

    private void seedProperty12() {
        Property p = Property.builder()
            .name("Bentota River House").city("Bentota").destination("Bentota").address("River Avenue, Bentota, Sri Lanka")
            .propertyType("Guesthouse").badge("Superhost").latitude(6.4271).longitude(79.9977)
            .imageSrc(IMG + "/v1778126098/properties/fhgr2dqufndugvpimexq.jpg")
            .galleryImages(String.join(",", IMG+"/v1778126086/properties/qn7uqn1b3wqstjwg1gpv.jpg", IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg", IMG+"/v1778126087/properties/yjgtu6fcfdoctjhhd2xp.jpg", IMG+"/v1778126090/properties/bafzr21edx4pzzjtuepp.jpg"))
            .description("A charming river house where the Bentota River meets the Indian Ocean. Kayak, fish, and birdwatch from your private jetty.")
            .hostName("Malini Jayasuriya").hostBio("Superhost · 6 years experience").hostYears(6).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("4000")).averageRating(4.94).reviewCount(72).published(true)
            .amenities("Wifi:Free WiFi,Waves:River Pool,Wind:Air Conditioning,Utensils:Home Cooking,Bike:Kayak Rental,ShieldCheck:Night Security,Car:Beach Shuttle")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Riverfront Suite").roomType("SUITE").maxOccupancy(2).sqft(420).bedType("1 King Bed").pricePerNight(new BigDecimal("42000")).originalPrice(new BigDecimal("50000")).tag("Popular").features("River views,Private balcony,Writing desk").imageSrc(IMG+"/v1778126085/properties/oj7bhzl7lfgqeuiznldp.jpg").available(true).build(),
            Room.builder().property(p).name("Garden Room").roomType("DOUBLE").maxOccupancy(2).sqft(300).bedType("1 Queen Bed").pricePerNight(new BigDecimal("32000")).tag("Refundable").features("Garden access,Hammock").imageSrc(IMG+"/v1778126089/properties/exgvkdnoiawfizd8u03s.jpg").available(true).build()
        ));
    }

    private void seedPromoCodes() {
        promoCodeRepository.saveAll(List.of(
            PromoCode.builder()
                .code("WELCOME10")
                .description("Welcome discount — 10% off your first booking")
                .discountPercent(new BigDecimal("10.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .maxUses(500)
                .currentUses(0)
                .active(true)
                .build(),
            PromoCode.builder()
                .code("SUMMER25")
                .description("Summer special — 25% off on any property")
                .discountPercent(new BigDecimal("25.00"))
                .validFrom(LocalDate.of(2026, 5, 1))
                .validTo(LocalDate.of(2026, 8, 31))
                .maxUses(200)
                .currentUses(0)
                .active(true)
                .build(),
            PromoCode.builder()
                .code("PRIMESTAY15")
                .description("Exclusive Prime Stay loyalty discount — 15% off")
                .discountPercent(new BigDecimal("15.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2027, 12, 31))
                .maxUses(null)
                .currentUses(0)
                .active(true)
                .build(),
            PromoCode.builder()
                .code("KANDY20")
                .description("20% off for Kandy Hilltop Luxury Villa only")
                .discountPercent(new BigDecimal("20.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .maxUses(100)
                .currentUses(0)
                .active(true)
                .propertyId(3L)
                .build(),
            PromoCode.builder()
                .code("EXPIRED5")
                .description("Expired test code — should not work")
                .discountPercent(new BigDecimal("5.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .validTo(LocalDate.of(2025, 12, 31))
                .maxUses(10)
                .currentUses(0)
                .active(true)
                .build()
        ));
    }
}
