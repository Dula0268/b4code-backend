package com.hospitality.service;

import com.hospitality.dao.BookingRepository;
import com.hospitality.dao.GuestPropertyRepository;
import com.hospitality.dao.ReviewRepository;
import com.hospitality.dao.RoomRepository;
import com.hospitality.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Seeds review data for properties.
 * Creates bookings first (reviews require a booking), then creates reviews.
 * Updates property averageRating and reviewCount after seeding.
 *
 * Runs AFTER PropertySeeder (Order 20 > Order 10).
 */
@Component
@Order(20)
public class ReviewSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReviewSeeder.class);
    private final GuestPropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final Random random = new Random(42); // Fixed seed for reproducible data

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
        null, null, null // Some reviews don't have owner replies
    };

    public ReviewSeeder(
            @Qualifier("guestPropertyRepository") GuestPropertyRepository propertyRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository) {
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        if (reviewRepository.count() > 0) {
            log.info("✅ Reviews already seeded, skipping");
            return;
        }

        List<GuestProperty> properties = propertyRepository.findAll().stream()
                .filter(GuestProperty::getPublished)
                .toList();

        if (properties.isEmpty()) {
            log.warn("⚠️ No published properties found — skipping review seeder");
            return;
        }

        log.info("🌱 Seeding reviews for {} properties...", properties.size());

        for (GuestProperty property : properties) {
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

        log.info("✅ Review seeding complete");
    }
}
