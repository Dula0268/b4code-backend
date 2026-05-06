package com.b4code.backend.common.config;

import com.b4code.backend.modules.admin.dao.AdminUserRepository;
import com.b4code.backend.modules.admin.enums.UserRole;
import com.b4code.backend.modules.admin.enums.UserStatus;
import com.b4code.backend.modules.admin.models.AdminUser;
import com.b4code.backend.modules.auth.entity.User;
import com.b4code.backend.modules.auth.repository.UserRepository;
import com.b4code.backend.modules.guest.dao.*;
import com.b4code.backend.modules.guest.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;

    @Override
    public void run(String... args) {
        // ── Seed auth users table (for login) ──────────────────────────────
        if (userRepository.findByEmail("admin@primestay.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@primestay.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setRole(User.Role.ADMIN);

            admin.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: admin@primestay.com");
        }

        // ── Seed admin_users table (for admin user management module) ───────
        if (adminUserRepository.count() == 0) {
            seedAdminUser("Sarah",  "Jenkins", "sarah.j@primestay.com",    UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Mike",   "Ross",    "mike.ross@primestay.com",  UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("John",   "Doe",     "john.d@gmail.com",         UserRole.STAFF, UserStatus.SUSPENDED);
            seedAdminUser("Emily",  "Chen",    "emily.chen@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Aisha",  "Kumar",   "aisha.k@primestay.com",    UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Nina",   "Patel",   "nina.patel@primestay.com", UserRole.OWNER, UserStatus.ACTIVE);
            seedAdminUser("Daniel", "Osei",    "daniel.o@primestay.com",   UserRole.STAFF, UserStatus.ACTIVE);
            seedAdminUser("Priya",  "Sharma",  "priya.s@primestay.com",    UserRole.OWNER, UserStatus.SUSPENDED);
            System.out.println("✅ Sample admin_users seeded (8 records)");
        }

        // ── Seed guest module data (properties, rooms, bookings, reviews, messages) ─────
        // Always empty the tables first to start fresh and apply new image URLs
        messageRepository.deleteAll();
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        propertyRepository.deleteAll();
        
        seedGuestData();
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

    private void seedGuestData() {
        // ── Seed Properties ───────────────────────────────────────────────
        Property prop1 = Property.builder()
                .name("Sunset Peak Resort")
                .city("Colombo")
                .address("123 Ocean View Lane, Colombo, Sri Lanka")
                .latitude(6.9271)
                .longitude(80.7789)
                .imageSrc("https://res.cloudinary.com/demo/image/upload/v1652343545/samples/landscapes/beach-boat.jpg")
                .description("Experience luxury beachfront living at Sunset Peak Resort with stunning ocean views and world-class amenities.")
                .averageRating(4.8)
                .reviewCount(127)
                .published(true)
                .build();
        prop1 = propertyRepository.save(prop1);

        Property prop2 = Property.builder()
                .name("Mountain View Villa")
                .city("Kandy")
                .address("456 Hill Station Road, Kandy, Sri Lanka")
                .latitude(7.2906)
                .longitude(80.6337)
                .imageSrc("https://res.cloudinary.com/demo/image/upload/v1652343545/samples/landscapes/nature-mountains.jpg")
                .description("Nestled in the misty mountains of Kandy, our villa offers tranquility and breathtaking views.")
                .averageRating(4.6)
                .reviewCount(89)
                .published(true)
                .build();
        prop2 = propertyRepository.save(prop2);

        Property prop3 = Property.builder()
                .name("Beach Paradise Bungalow")
                .city("Galle")
                .address("789 Coastal Lane, Galle, Sri Lanka")
                .latitude(6.0328)
                .longitude(80.2167)
                .imageSrc("https://res.cloudinary.com/demo/image/upload/cld-sample-2.jpg")
                .description("Your ultimate beach getaway with direct access to pristine sandy beaches and crystal-clear waters.")
                .averageRating(4.9)
                .reviewCount(156)
                .published(true)
                .build();
        prop3 = propertyRepository.save(prop3);

        // ── Seed Rooms ────────────────────────────────────────────────────
        // Rooms for Sunset Peak Resort
        Room room1 = Room.builder()
                .property(prop1)
                .name("Deluxe Ocean View")
                .roomType("DOUBLE")
                .maxOccupancy(2)
                .pricePerNight(new BigDecimal("35000"))
                .imageSrc("https://res.cloudinary.com/demo/image/upload/cld-sample.jpg")
                .amenities("AC,WiFi,Balcony,TV,Minibar,Safe")
                .available(true)
                .build();
        room1 = roomRepository.save(room1);

        Room room2 = Room.builder()
                .property(prop1)
                .name("Premium Suite")
                .roomType("SUITE")
                .maxOccupancy(4)
                .pricePerNight(new BigDecimal("52000"))
                .imageSrc("https://res.cloudinary.com/demo/image/upload/cld-sample-3.jpg")
                .amenities("AC,WiFi,Balcony,TV,Minibar,Safe,Jacuzzi,KitchenArea")
                .available(true)
                .build();
        room2 = roomRepository.save(room2);

        Room room3 = Room.builder()
                .property(prop1)
                .name("Standard Room")
                .roomType("SINGLE")
                .maxOccupancy(1)
                .pricePerNight(new BigDecimal("22000"))
                .imageSrc("https://res.cloudinary.com/demo/image/upload/cld-sample-4.jpg")
                .amenities("AC,WiFi,TV,Bathroom")
                .available(true)
                .build();
        room3 = roomRepository.save(room3);

        // Rooms for Mountain View Villa
        Room room4 = Room.builder()
                .property(prop2)
                .name("Mountain View Room")
                .roomType("DOUBLE")
                .maxOccupancy(2)
                .pricePerNight(new BigDecimal("28000"))
                .imageSrc("https://res.cloudinary.com/demo/image/upload/v1652343545/samples/landscapes/architecture-signs.jpg")
                .amenities("AC,WiFi,Fireplace,Balcony,TV")
                .available(true)
                .build();
        room4 = roomRepository.save(room4);

        Room room5 = Room.builder()
                .property(prop2)
                .name("Hill Station Cottage")
                .roomType("SUITE")
                .maxOccupancy(3)
                .pricePerNight(new BigDecimal("42000"))
                .imageSrc("https://res.cloudinary.com/demo/image/upload/cld-sample-5.jpg")
                .amenities("AC,WiFi,Fireplace,Balcony,TV,KitchenArea,Jacuzzi")
                .available(true)
                .build();
        room5 = roomRepository.save(room5);

        // Rooms for Beach Paradise Bungalow
        Room room6 = Room.builder()
                .property(prop3)
                .name("Beach Front Bungalow")
                .roomType("SUITE")
                .maxOccupancy(2)
                .pricePerNight(new BigDecimal("48000"))
                .imageSrc("https://res.cloudinary.com/demo/image/upload/v1652343545/samples/landscapes/beach-boat.jpg")
                .amenities("AC,WiFi,Balcony,TV,DirectBeachAccess,Minibar")
                .available(true)
                .build();
        room6 = roomRepository.save(room6);

        // ── Seed Bookings ─────────────────────────────────────────────────
        Booking booking1 = Booking.builder()
                .room(room1)
                .guestName("Kasun Perera")
                .guestEmail("guest@primestay.com")
                .guestPhone("+94701234567")
                .checkIn(LocalDate.of(2026, 10, 15))
                .checkOut(LocalDate.of(2026, 10, 20))
                .guestCount(2)
                .totalAmount(new BigDecimal("175000"))
                .taxAmount(new BigDecimal("19250"))
                .promoCode(null)
                .discountAmount(BigDecimal.ZERO)
                .status(Booking.BookingStatus.CONFIRMED)
                .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                .confirmationNumber("B4C-CONF-001")
                .build();
        booking1 = bookingRepository.save(booking1);

        Booking booking2 = Booking.builder()
                .room(room2)
                .guestName("Amara Silva")
                .guestEmail("guest@primestay.com")
                .guestPhone("+94702345678")
                .checkIn(LocalDate.of(2026, 11, 5))
                .checkOut(LocalDate.of(2026, 11, 10))
                .guestCount(3)
                .totalAmount(new BigDecimal("260000"))
                .taxAmount(new BigDecimal("28600"))
                .promoCode(null)
                .discountAmount(BigDecimal.ZERO)
                .status(Booking.BookingStatus.COMPLETED)
                .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                .confirmationNumber("B4C-CONF-002")
                .build();
        booking2 = bookingRepository.save(booking2);

        Booking booking3 = Booking.builder()
                .room(room4)
                .guestName("Ismail Khan")
                .guestEmail("guest@primestay.com")
                .guestPhone("+94703456789")
                .checkIn(LocalDate.of(2026, 10, 22))
                .checkOut(LocalDate.of(2026, 10, 25))
                .guestCount(2)
                .totalAmount(new BigDecimal("84000"))
                .taxAmount(new BigDecimal("9240"))
                .promoCode(null)
                .discountAmount(BigDecimal.ZERO)
                .status(Booking.BookingStatus.CONFIRMED)
                .paymentMethod(Booking.PaymentMethod.PAY_AT_PROPERTY)
                .confirmationNumber("B4C-CONF-003")
                .build();
        booking3 = bookingRepository.save(booking3);

        Booking booking4 = Booking.builder()
                .room(room6)
                .guestName("Priya Gunawardana")
                .guestEmail("guest@primestay.com")
                .guestPhone("+94704567890")
                .checkIn(LocalDate.of(2026, 11, 15))
                .checkOut(LocalDate.of(2026, 11, 22))
                .guestCount(2)
                .totalAmount(new BigDecimal("336000"))
                .taxAmount(new BigDecimal("36960"))
                .promoCode(null)
                .discountAmount(BigDecimal.ZERO)
                .status(Booking.BookingStatus.CONFIRMED)
                .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                .confirmationNumber("B4C-CONF-004")
                .build();
        booking4 = bookingRepository.save(booking4);

        // ── Seed Reviews ──────────────────────────────────────────────────
        Review review1 = Review.builder()
                .booking(booking1)
                .property(prop1)
                .guestName("Kasun Perera")
                .overallRating(5)
                .cleanlinessRating(5)
                .accuracyRating(4)
                .communicationRating(5)
                .locationRating(5)
                .valueRating(4)
                .comment("Excellent stay! The views were breathtaking and the staff was very helpful. Highly recommended.")
                .photoUrls("https://res.cloudinary.com/demo/image/upload/cld-sample.jpg,https://res.cloudinary.com/demo/image/upload/cld-sample-2.jpg")
                .isVerifiedStay(true)
                .build();
        reviewRepository.save(review1);

        Review review2 = Review.builder()
                .booking(booking2)
                .property(prop1)
                .guestName("Amara Silva")
                .overallRating(4)
                .cleanlinessRating(4)
                .accuracyRating(5)
                .communicationRating(4)
                .locationRating(5)
                .valueRating(3)
                .comment("Great location and comfortable rooms. Food could have been better. Overall good experience.")
                .photoUrls("https://res.cloudinary.com/demo/image/upload/cld-sample-3.jpg,https://res.cloudinary.com/demo/image/upload/cld-sample-4.jpg")
                .isVerifiedStay(true)
                .build();
        reviewRepository.save(review2);

        Review review3 = Review.builder()
                .booking(booking3)
                .property(prop2)
                .guestName("Ismail Khan")
                .overallRating(5)
                .cleanlinessRating(5)
                .accuracyRating(5)
                .communicationRating(5)
                .locationRating(4)
                .valueRating(5)
                .comment("Perfect getaway! The mountain views are stunning and the service is impeccable.")
                .photoUrls("https://res.cloudinary.com/demo/image/upload/cld-sample-5.jpg")
                .isVerifiedStay(true)
                .build();
        reviewRepository.save(review3);

        // ── Seed Messages ─────────────────────────────────────────────────
        Message msg1 = Message.builder()
                .booking(booking1)
                .senderType(Message.SenderType.GUEST)
                .senderName("Kasun Perera")
                .content("Hi, can we do early check-in? I'll arrive by noon.")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(24))
                .build();
        messageRepository.save(msg1);

        Message msg2 = Message.builder()
                .booking(booking1)
                .senderType(Message.SenderType.PROPERTY)
                .senderName("Sunset Peak Resort")
                .content("Of course! We can accommodate early check-in from 11 AM onwards. Looking forward to your arrival.")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(23))
                .build();
        messageRepository.save(msg2);

        Message msg3 = Message.builder()
                .booking(booking1)
                .senderType(Message.SenderType.GUEST)
                .senderName("Kasun Perera")
                .content("Thank you! Do you have any recommendations for dining nearby?")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(22))
                .build();
        messageRepository.save(msg3);

        Message msg4 = Message.builder()
                .booking(booking1)
                .senderType(Message.SenderType.PROPERTY)
                .senderName("Sunset Peak Resort")
                .content("Yes! I'd recommend the beachfront restaurants nearby. Our concierge can make reservations for you.")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(21))
                .build();
        messageRepository.save(msg4);

        Message msg5 = Message.builder()
                .booking(booking3)
                .senderType(Message.SenderType.GUEST)
                .senderName("Ismail Khan")
                .content("Hi, can I request late checkout on the last day?")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(12))
                .build();
        messageRepository.save(msg5);

        Message msg6 = Message.builder()
                .booking(booking3)
                .senderType(Message.SenderType.PROPERTY)
                .senderName("Mountain View Villa")
                .content("Absolutely! Late checkout until 2 PM is available at no extra charge.")
                .isRead(true)
                .sentAt(LocalDateTime.now().minusHours(11))
                .build();
        messageRepository.save(msg6);

        System.out.println("✅ Guest module data seeded:");
        System.out.println("   - 3 Properties");
        System.out.println("   - 6 Rooms");
        System.out.println("   - 4 Bookings");
        System.out.println("   - 3 Reviews");
        System.out.println("   - 6 Messages");
    }
}