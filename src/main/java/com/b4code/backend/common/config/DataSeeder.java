package com.b4code.backend.common.config;

import com.b4code.backend.dao.*;
import com.b4code.backend.models.*;
import com.b4code.backend.models.enums.*;
import com.b4code.backend.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(UserRepository userRepository,
                      PropertyRepository propertyRepository,
                      RoomRepository roomRepository,
                      AmenityRepository amenityRepository,
                      BookingRepository bookingRepository,
                      ReviewRepository reviewRepository,
                      PromotionRepository promotionRepository,
                      PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
        this.amenityRepository = amenityRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.promotionRepository = promotionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        seedPromotions();
        seedGuestData();
        seedCoreUsers();
    }

    private void seedPromotions() {
        if (promotionRepository.count() == 0) {
            promotionRepository.save(Promotion.builder()
                    .code("WELCOME10")
                    .discountPercentage(10.0)
                    .validFrom(LocalDate.now().minusDays(1))
                    .validUntil(LocalDate.now().plusMonths(3))
                    .isActive(true)
                    .build());
            log.info("Seeded promotions.");
        }
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
        log.info("🧹 Dropping stale columns to avoid constraint violations...");
        jdbcTemplate.execute("ALTER TABLE owner.rooms DROP COLUMN IF EXISTS name");
        jdbcTemplate.execute("ALTER TABLE guest.bookings DROP COLUMN IF EXISTS created_at");
        jdbcTemplate.execute("ALTER TABLE guest.bookings DROP COLUMN IF EXISTS updated_at");
        jdbcTemplate.execute("ALTER TABLE guest.bookings DROP COLUMN IF EXISTS status");
        jdbcTemplate.execute("ALTER TABLE guest.bookings DROP COLUMN IF EXISTS booking_status");
        jdbcTemplate.execute("ALTER TABLE guest.reviews DROP COLUMN IF EXISTS created_at");
        jdbcTemplate.execute("ALTER TABLE guest.reviews DROP COLUMN IF EXISTS updated_at");
        jdbcTemplate.execute("ALTER TABLE owner.rooms DROP COLUMN IF EXISTS created_at");
        jdbcTemplate.execute("ALTER TABLE owner.rooms DROP COLUMN IF EXISTS updated_at");
        jdbcTemplate.execute("ALTER TABLE owner.properties DROP COLUMN IF EXISTS created_at");
        jdbcTemplate.execute("ALTER TABLE owner.properties DROP COLUMN IF EXISTS updated_at");
        jdbcTemplate.execute("ALTER TABLE owner.amenity DROP COLUMN IF EXISTS created_at");
        jdbcTemplate.execute("ALTER TABLE owner.amenity DROP COLUMN IF EXISTS updated_at");
        jdbcTemplate.execute("ALTER TABLE owner.images DROP COLUMN IF EXISTS created_at");
        jdbcTemplate.execute("ALTER TABLE owner.images DROP COLUMN IF EXISTS updated_at");

        // Drop stale check constraints on rooms — they were generated from an older version
        // of the Java enum and will reject new enum values (e.g. PREMIER_ROOM, LAGOON_VIEW_ROOM).
        // The constraint will be recreated correctly by Hibernate on the next schema update.
        log.info("🔧 Dropping stale rooms check constraints...");
        jdbcTemplate.execute("ALTER TABLE owner.rooms DROP CONSTRAINT IF EXISTS rooms_room_type_check");
        jdbcTemplate.execute("ALTER TABLE owner.rooms DROP CONSTRAINT IF EXISTS rooms_bed_type_check");

        log.info("🗑️  Truncating existing data...");
        jdbcTemplate.execute("TRUNCATE TABLE app_auth.users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE owner.properties RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE guest.reviews RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE guest.bookings RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE owner.rooms RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE owner.images RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE owner.amenity RESTART IDENTITY CASCADE");

        log.info("🌱 Seeding 8 luxury Sri Lankan properties...");
        seedAllProperties();
        log.info("✅ Property seeding complete.");
    }

    private void seedAllProperties() {
        // ── 6 owners ──────────────────────────────────────────────────────────
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

        // ── 2 distinct guest reviewers ────────────────────────────────────────
        User guest1 = userRepository.findByEmail("guest@primestay.com").orElseGet(() -> {
            User g = new User();
            g.setEmail("guest@primestay.com");
            g.setPasswordHash(passwordEncoder.encode("guest123"));
            g.setFirstName("John");
            g.setLastName("Doe");
            g.setRole(UserRole.GUEST);
            g.setStatus(UserStatus.ACTIVE);
            return userRepository.save(g);
        });

        User guest2 = userRepository.findByEmail("guest2@primestay.com").orElseGet(() -> {
            User g = new User();
            g.setEmail("guest2@primestay.com");
            g.setPasswordHash(passwordEncoder.encode("guest123"));
            g.setFirstName("Priya");
            g.setLastName("Fernando");
            g.setRole(UserRole.GUEST);
            g.setStatus(UserStatus.ACTIVE);
            return userRepository.save(g);
        });

        // ══════════════════════════════════════════════════════════════════════
        // PROPERTY DATA — 8 properties
        // ══════════════════════════════════════════════════════════════════════
        String[] titles = {
            "Shangri-La Colombo",
            "Heritance Kandalama",
            "Anantara Peace Haven Tangalle Resort",
            "Amangalla",
            "Ceylon Tea Trails – Norwood Bungalow",
            "Hilton Yala Resort",
            "Shangri-La Hambantota",
            "Anantara Kalutara Resort"
        };

        String[] cities = {
            "Colombo", "Dambulla", "Tangalle", "Galle Fort",
            "Hatton", "Yala", "Hambantota", "Kalutara"
        };

        String[] descriptions = {
            "A flagship city luxury hotel rising above the Colombo skyline with panoramic harbour views, world-class dining and an award-winning spa. Perfect for business travellers and city explorers.",
            "Carved into a dramatic cliff face overlooking the ancient Kandalama tank, this eco-sensitive resort designed by Geoffrey Bawa immerses guests in lush jungle and archaeological wonder.",
            "A secluded beachfront retreat on Sri Lanka's south coast, offering infinity pools, over-water bungalows and direct access to pristine Tangalle beach — ideal for romantic escapes.",
            "Housed in a 300-year-old Dutch fortress, this heritage luxury hotel in Galle Fort blends colonial grandeur with modern comfort. UNESCO-listed surroundings make every evening magical.",
            "Nestled within working tea estates at 4 000 ft, this intimate bungalow offers colonial-era charm, breathtaking valley vistas and bespoke tea experiences in Sri Lanka's hill country.",
            "A premier eco-luxury lodge on the edge of Yala National Park, delivering thrilling safari mornings, tented suites and exclusive wildlife encounters in Sri Lanka's wildest corner.",
            "A grand beachfront resort on Hambantota's golden sands featuring an 18-hole championship golf course, multiple pools and a world-class spa — where sport meets seaside luxury.",
            "Set on a private lagoon at the mouth of the Kalu River, this serene resort offers overwater villas, Ayurvedic treatments and lush tropical gardens just 45 minutes south of Colombo."
        };

        // 8 unique property cover images (Unsplash — luxury hotel/resort theme)
        String[] propertyImages = {
            "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=900&auto=format&fit=crop", // 1 Colombo city luxury
            "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=900&auto=format&fit=crop", // 2 Jungle/lake eco resort
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=900&auto=format&fit=crop", // 3 Beach paradise
            "https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=900&auto=format&fit=crop", // 4 Heritage colonial
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=900&auto=format&fit=crop", // 5 Tea hills misty
            "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=900&auto=format&fit=crop", // 6 Wildlife/safari tents
            "https://images.unsplash.com/photo-1540541338287-41700207dee6?w=900&auto=format&fit=crop", // 7 Golf/beach resort
            "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=900&auto=format&fit=crop"  // 8 Lagoon overwater
        };

        // 16 unique room images (2 per property, never reused)
        String[][] roomImages = {
            // Property 1 — Shangri-La Colombo
            {
                "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1616594039964-ae9021a400a0?w=800&auto=format&fit=crop"
            },
            // Property 2 — Heritance Kandalama
            {
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&auto=format&fit=crop"
            },
            // Property 3 — Anantara Tangalle
            {
                "https://images.unsplash.com/photo-1602002418082-a4443e081dd1?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1560347876-aeef00ee58a1?w=800&auto=format&fit=crop"
            },
            // Property 4 — Amangalla
            {
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&auto=format&fit=crop"
            },
            // Property 5 — Ceylon Tea Trails
            {
                "https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1588880331179-bc9b93a8cb5e?w=800&auto=format&fit=crop"
            },
            // Property 6 — Hilton Yala
            {
                "https://images.unsplash.com/photo-1565538810643-b5bdb714032a?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1584132869994-873f9363a562?w=800&auto=format&fit=crop"
            },
            // Property 7 — Shangri-La Hambantota
            {
                "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800&auto=format&fit=crop"
            },
            // Property 8 — Anantara Kalutara
            {
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800&auto=format&fit=crop"
            }
        };

        // 2 unique amenities per property (tailored to category)
        String[][] amenities = {
            {"Rooftop Infinity Pool",   "24-Hour Concierge"},          // 1 Colombo city
            {"Jungle Nature Walks",     "Geoffrey Bawa Heritage Tour"}, // 2 Kandalama eco
            {"Private Beach Access",    "Over-Water Spa Treatment"},    // 3 Tangalle beach
            {"Colonial Heritage Tour",  "Galle Fort Guided Walk"},      // 4 Amangalla
            {"Guided Tea Plucking",     "Fireplace Butler Service"},     // 5 Tea Trails
            {"Morning Safari Drive",    "Wildlife Photography Workshop"},// 6 Yala safari
            {"Championship Golf Course","Ocean-View Infinity Pool"},    // 7 Hambantota golf
            {"Ayurvedic Spa Retreat",   "Lagoon Kayaking Experience"}   // 8 Kalutara lagoon
        };

        // 2 unique room types per property
        RoomType[][] roomTypes = {
            {RoomType.DELUXE_ROOM,       RoomType.PREMIER_ROOM},
            {RoomType.SUPERIOR_ROOM,     RoomType.LUXURY_ROOM},
            {RoomType.GARDEN_VIEW_ROOM,  RoomType.OCEAN_VIEW_ROOM},
            {RoomType.BEDROOM_SUITE,     RoomType.GARDEN_SUITE},
            {RoomType.LUXURY_ROOM,       RoomType.MASTER_SUITE},
            {RoomType.DELUXE_ROOM,       RoomType.PREMIUM_ROOM},
            {RoomType.PREMIER_OCEAN_VIEW,RoomType.EXECUTIVE_SUITE},
            {RoomType.GARDEN_VIEW_ROOM,  RoomType.LAGOON_VIEW_ROOM}
        };

        // 2 unique bed types per property
        BedType[][] bedTypes = {
            {BedType.QUEEN,  BedType.KING},
            {BedType.DOUBLE, BedType.KING},
            {BedType.QUEEN,  BedType.KING},
            {BedType.DOUBLE, BedType.KING},
            {BedType.QUEEN,  BedType.KING},
            {BedType.TWIN,   BedType.KING},
            {BedType.QUEEN,  BedType.KING},
            {BedType.DOUBLE, BedType.KING}
        };

        // Price per night (LKR): room1, room2
        int[][] prices = {
            {32000, 55000},
            {28000, 48000},
            {35000, 62000},
            {45000, 75000},
            {22000, 38000},
            {30000, 52000},
            {40000, 68000},
            {33000, 58000}
        };

        // max occupancy per room
        int[][] occupancy = {
            {2, 3},
            {2, 3},
            {2, 4},
            {2, 3},
            {2, 2},
            {2, 4},
            {2, 3},
            {2, 4}
        };

        // 2 unique review comments per property (guest1 & guest2 alternate)
        String[][] reviewComments = {
            // 1 Colombo
            {
                "Absolutely breathtaking views of the Colombo harbour from the rooftop pool. Service was impeccable — every detail thought of. Will be back!",
                "The rooms are elegantly designed and spotlessly clean. Loved the rooftop dining experience. A genuine five-star city retreat."
            },
            // 2 Kandalama
            {
                "Waking up to jungle mist and monkeys outside the window is a memory I'll treasure. Bawa's architecture is awe-inspiring — truly one-of-a-kind.",
                "The eco-resort ethos is genuine — nature is all around you yet comfort never compromised. The Sigiriya day-trip package was excellent value."
            },
            // 3 Tangalle
            {
                "The beachfront villa exceeded every expectation. Falling asleep to waves and waking to a private sunrise on the Indian Ocean was magical.",
                "Exceptional spa treatments right on the beach. The staff were warm and anticipatory — nothing was too much trouble. Honeymoon perfection!"
            },
            // 4 Amangalla
            {
                "Stepping inside Amangalla is like stepping back 300 years — polished teak floors, colonial antiques and utter serenity inside the Galle Fort walls.",
                "The heritage ambiance is unmatched anywhere in Sri Lanka. The candlelit dinner in the old Dutch courtyard was the most romantic evening of our trip."
            },
            // 5 Tea Trails
            {
                "Tea-plucking at dawn with sweeping valley views, then a fireside dinner prepared by the butler — Ceylon Tea Trails is something deeply special.",
                "Total seclusion in the misty hill country. We unplugged completely and rediscovered what a slow, intentional holiday feels like. Highly recommended."
            },
            // 6 Yala
            {
                "Spotted leopards on three consecutive morning safaris! The tented suites blend luxury with a genuine bush atmosphere. Thrilling from start to finish.",
                "Yala Hilton surprised us with how luxurious a wildlife stay can be. Cold towels after safaris, gourmet bush dinners — the details really set it apart."
            },
            // 7 Hambantota
            {
                "Played nine holes at sunrise with no one else on the course and the ocean as a backdrop. The resort's scale and facilities are simply world-class.",
                "A stunning combination of golf, beach and fine dining. The infinity pool over-looking the Indian Ocean at sunset is one of those views you never forget."
            },
            // 8 Kalutara
            {
                "The lagoon-side overwater villa was our honeymoon dream come true. Waking to still water and birdsong just 45 minutes from Colombo is a revelation.",
                "Ayurvedic consultations, daily yoga and an utterly tranquil lagoon setting made this the most restorative stay I've ever had. Will return every year."
            }
        };

        // Ratings: guest1 rating, guest2 rating  (used to compute average)
        int[][] ratings = {
            {5, 5},
            {5, 4},
            {5, 5},
            {4, 5},
            {5, 4},
            {5, 4},
            {4, 5},
            {5, 5}
        };

        // Misc flags (freeCancellation, breakfastIncluded, petFriendly, accessibility)
        boolean[][] flags = {
            {true,  false, false, false},
            {false, true,  false, true },
            {true,  true,  false, false},
            {false, false, false, true },
            {true,  true,  false, false},
            {false, false, false, false},
            {true,  false, true,  false},
            {true,  true,  false, true }
        };

        // lat/lng approximate for Sri Lanka cities
        double[] latitudes  = {6.9271, 7.9186, 6.0179, 6.0276, 6.9002, 6.3730, 6.1241, 6.5854};
        double[] longitudes = {79.8612, 80.7540, 80.7994, 80.2167, 80.4982, 81.3230, 81.1185, 79.9607};

        User[] reviewUsers = {guest1, guest2};

        for (int i = 0; i < 8; i++) {
            User owner = owners.get(i % 6);

            // ── Save property ────────────────────────────────────────────────
            Property p = Property.builder()
                    .name(titles[i])
                    .city(cities[i])
                    .country("Sri Lanka")
                    .addressLine1(cities[i] + ", Sri Lanka")
                    .latitude(latitudes[i])
                    .longitude(longitudes[i])
                    .ownerId(owner.getId())
                    .description(descriptions[i])
                    .freeCancellation(flags[i][0])
                    .breakfastIncluded(flags[i][1])
                    .petFriendly(flags[i][2])
                    .accessibility(flags[i][3])
                    .build();
            p = propertyRepository.save(p);

            // ── 2 unique amenities ───────────────────────────────────────────
            Set<Amenity> ams = new HashSet<>();
            ams.add(Amenity.builder().name(amenities[i][0]).property(p).build());
            ams.add(Amenity.builder().name(amenities[i][1]).property(p).build());
            p.setAmenities(ams);

            // ── Images: 1 property cover + 2 room images ────────────────────
            List<Image> images = new ArrayList<>();
            images.add(Image.builder()
                    .property(p)
                    .url(propertyImages[i])
                    .type(ImageType.PROPERTY)
                    .build());
            images.add(Image.builder()
                    .property(p)
                    .url(roomImages[i][0])
                    .type(ImageType.ROOM)
                    .build());
            images.add(Image.builder()
                    .property(p)
                    .url(roomImages[i][1])
                    .type(ImageType.ROOM)
                    .build());
            p.setImages(images);
            p = propertyRepository.save(p);

            // ── 2 unique rooms ───────────────────────────────────────────────
            List<Room> createdRooms = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                Room r = Room.builder()
                        .property(p)
                        .roomType(roomTypes[i][j])
                        .maxOccupancy(occupancy[i][j])
                        .bedType(bedTypes[i][j])
                        .pricePerNight(new BigDecimal(prices[i][j]))
                        .image(p.getImages().get(j + 1)) // index 0 = property cover, 1 & 2 = room images
                        .build();
                createdRooms.add(roomRepository.save(r));
            }

            // ── 2 unique reviews (one per guest, different rooms) ────────────
            int totalRating = 0;
            for (int r = 0; r < 2; r++) {
                // Dummy booking required because Review → OneToOne → Booking
                Booking dummyBooking = Booking.builder()
                        .room(createdRooms.get(r))          // r=0 → room1, r=1 → room2
                        .property(p)
                        .checkIn(LocalDate.now().minusDays(15 + r * 5L))
                        .checkOut(LocalDate.now().minusDays(12 + r * 5L))
                        .adults(2)
                        .children(0)
                        .totalAmount(new BigDecimal(prices[i][r] * 3))
                        .taxAmount(new BigDecimal(prices[i][r] / 10))
                        .promoCode("")
                        .paymentMethod(Booking.PaymentMethod.ONLINE_CARD)
                        .build();
                dummyBooking = bookingRepository.save(dummyBooking);

                int rating = ratings[i][r];
                totalRating += rating;

                Review review = Review.builder()
                        .booking(dummyBooking)
                        .property(p)
                        .guest(reviewUsers[r])
                        .overallRating(rating)
                        .comment(reviewComments[i][r])
                        .photoUrls(roomImages[i][r])
                        .build();
                reviewRepository.save(review);
            }

            // ── Update property with calculated rating & review count ────────
            double averageRating = (double) totalRating / 2;
            p.setAverageRating(averageRating);
            p.setReviewCount(2);
            propertyRepository.save(p);

            log.info("✅ Property {} seeded: {} | avg={} | reviews=2", i + 1, titles[i], averageRating);
        }

        log.info("🏨 All 8 properties seeded successfully.");
    }
}
