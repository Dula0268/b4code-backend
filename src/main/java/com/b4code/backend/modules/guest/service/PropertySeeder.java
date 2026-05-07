package com.b4code.backend.modules.guest.service;

import com.b4code.backend.modules.guest.dao.PropertyRepository;
import com.b4code.backend.modules.guest.dao.RoomRepository;
import com.b4code.backend.modules.guest.models.Property;
import com.b4code.backend.modules.guest.models.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(10)
public class PropertySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PropertySeeder.class);
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;

    // Cloudinary demo images - using Cloudinary's public demo account sample images
    // To use your own images:
    //   1. Create a Cloudinary account at https://cloudinary.com
    //   2. Upload your property images via dashboard or API
    //   3. Update these URLs with your cloud name and image IDs
    //   4. No frontend code changes needed — URLs come from database
    private static final String IMG = "https://res.cloudinary.com/demo/image/upload";

    public PropertySeeder(
            @Qualifier("guestPropertyRepository") PropertyRepository propertyRepository,
            RoomRepository roomRepository) {
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (propertyRepository.countByPublishedTrue() > 0) {
            log.info("✅ Properties already seeded, skipping");
            return;
        }
        log.info("🌱 Seeding properties...");
        seedAllProperties();
        log.info("✅ Property seeding complete");
    }

    private void seedAllProperties() {
        seedProperty1(); seedProperty2(); seedProperty3(); seedProperty4();
        seedProperty5(); seedProperty6(); seedProperty7(); seedProperty8();
        seedProperty9(); seedProperty10(); seedProperty11(); seedProperty12();
    }

    private void seedProperty1() {
        Property p = Property.builder()
            .name("Colombo Sky Residency").city("Colombo 3").address("32 Galle Road, Colombo 03, Sri Lanka")
            .propertyType("Apartment").badge("Superhost").latitude(6.9088).longitude(79.8543)
            .imageSrc(IMG + "/v1/samples/landscapes/architecture-signs")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/landscapes/girl-urban-view"))
            .description("Experience Colombo's vibrant city life from this sleek sky-high apartment. Floor-to-ceiling windows frame panoramic views of the Indian Ocean and the city skyline. Designed with minimalist luxury in mind.")
            .hostName("Priya Fernando").hostBio("Superhost · 5 years experience").hostYears(5).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("5000")).averageRating(4.92).reviewCount(148).published(true)
            .amenities("Wifi:Free High Speed WiFi,Wind:Air Conditioning,Waves:Rooftop Pool,Dumbbell:Fitness Center,Car:Valet Parking,Utensils:In-suite Kitchen,ShieldCheck:24hr Security,Coffee:Nespresso Machine")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Deluxe Ocean View Room").roomType("DOUBLE").maxOccupancy(2).sqft(480).bedType("1 King Bed").pricePerNight(new BigDecimal("25000")).originalPrice(new BigDecimal("28000")).tag("Refundable").features("Private Balcony,Nespresso Machine").imageSrc(IMG+"/v1/samples/landscapes/beach-boat").available(true).build(),
            Room.builder().property(p).name("Panoramic Grand Suite").roomType("SUITE").maxOccupancy(4).sqft(650).bedType("2 Queen Beds").pricePerNight(new BigDecimal("35000")).tag("Popular").features("Floor-to-ceiling windows,Separate living area").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build(),
            Room.builder().property(p).name("Sky Penthouse").roomType("SUITE").maxOccupancy(2).sqft(900).bedType("1 King Bed").pricePerNight(new BigDecimal("55000")).features("Private terrace,Marble bathroom,Butler service").imageSrc(IMG+"/v1/samples/landscapes/girl-urban-view").available(true).build()
        ));
    }

    private void seedProperty2() {
        Property p = Property.builder()
            .name("Galle Fort Heritage Cottage").city("Galle Fort").address("14 Church St, Galle Fort, Sri Lanka")
            .propertyType("Guesthouse").latitude(6.0328).longitude(80.2170)
            .imageSrc(IMG + "/v1/samples/landscapes/nature-mountains")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/architecture-signs", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/landscapes/girl-urban-view"))
            .description("Step back in time in this beautifully restored Dutch colonial cottage within the UNESCO-listed Galle Fort. Original 18th-century architecture blends seamlessly with modern comfort.")
            .hostName("Chamari De Silva").hostBio("Superhost · 7 years experience").hostYears(7).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("4000")).averageRating(4.85).reviewCount(92).published(true)
            .amenities("Wifi:Free WiFi,Wind:Air Conditioning,BookOpen:Library,Coffee:Garden Café,ShieldCheck:24hr Security,Bike:Bicycle Rental")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Heritage Garden Room").roomType("DOUBLE").maxOccupancy(2).sqft(320).bedType("1 King Bed").pricePerNight(new BigDecimal("35000")).originalPrice(new BigDecimal("42000")).tag("Refundable").features("Courtyard access,Antique furnishings").imageSrc(IMG+"/v1/samples/food/spices").available(true).build(),
            Room.builder().property(p).name("Fort View Loft").roomType("DOUBLE").maxOccupancy(2).sqft(280).bedType("1 Queen Bed").pricePerNight(new BigDecimal("40000")).tag("Popular").features("Fort wall views,Skylight bathroom").imageSrc(IMG+"/v1/samples/landscapes/girl-urban-view").available(true).build()
        ));
    }

    private void seedProperty3() {
        Property p = Property.builder()
            .name("Kandy Hilltop Luxury Villa").city("Kandy").address("7 Rajapihilla Mawatha, Kandy, Sri Lanka")
            .propertyType("Villa").badge("Guest favorite").latitude(7.2906).longitude(80.6337)
            .imageSrc(IMG + "/v1/samples/landscapes/girl-urban-view")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/architecture-signs", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/food/spices"))
            .description("Perched above the misty hills overlooking the sacred Kandy Lake and the Temple of the Tooth, this private luxury villa offers an unparalleled blend of nature and elegance.")
            .hostName("Roshan Mendis").hostBio("Superhost · 6 years experience").hostYears(6).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("8000")).averageRating(5.0).reviewCount(67).published(true)
            .amenities("Wifi:Free High Speed WiFi,Waves:Infinity Pool,Wind:Air Conditioning,Utensils:Private Chef,Car:Chauffeur Service,Dumbbell:Yoga Pavilion,ShieldCheck:24hr Security,Coffee:Butler Service")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Master Lake View Suite").roomType("SUITE").maxOccupancy(2).sqft(700).bedType("1 King Bed").pricePerNight(new BigDecimal("75000")).originalPrice(new BigDecimal("90000")).tag("Popular").features("Lake-facing balcony,Rain shower,Bathtub").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build(),
            Room.builder().property(p).name("Garden Pavilion Suite").roomType("SUITE").maxOccupancy(4).sqft(950).bedType("2 King Beds").pricePerNight(new BigDecimal("120000")).features("Private garden,Outdoor soaking tub,Kitchenette").imageSrc(IMG+"/v1/samples/landscapes/beach-boat").available(true).build()
        ));
    }

    private void seedProperty4() {
        Property p = Property.builder()
            .name("Colombo Boutique Business Suite").city("Colombo 7").address("18 Ward Place, Colombo 07, Sri Lanka")
            .propertyType("Apartment").latitude(6.9060).longitude(79.8605)
            .imageSrc(IMG + "/v1/samples/food/spices")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/architecture-signs", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/landscapes/girl-urban-view", IMG+"/v1/samples/landscapes/nature-mountains"))
            .description("A refined urban retreat in the prestigious Colombo 7 enclave, designed for the modern business traveller.")
            .hostName("Dilrukshi Jayawardena").hostBio("Superhost · 4 years experience").hostYears(4).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("6000")).averageRating(4.75).reviewCount(53).published(true)
            .amenities("Wifi:Gigabit WiFi,Wind:Air Conditioning,Monitor:Work Desk & Monitor,Coffee:Nespresso Machine,Car:Parking,ShieldCheck:24hr Security")
            .build();
        p = propertyRepository.save(p);
        roomRepository.save(Room.builder().property(p).name("Executive Business Suite").roomType("SUITE").maxOccupancy(2).sqft(520).bedType("1 King Bed").pricePerNight(new BigDecimal("85000")).tag("Refundable").features("Dual monitor setup,Standing desk,Meeting table for 4").imageSrc(IMG+"/v1/samples/landscapes/architecture-signs").available(true).build());
    }

    private void seedProperty5() {
        Property p = Property.builder()
            .name("Negombo Beachside Retreat").city("Negombo").address("78 Lewis Place, Negombo, Sri Lanka")
            .propertyType("Hotel").badge("Superhost").latitude(7.2083).longitude(79.8358)
            .imageSrc(IMG + "/v1/samples/landscapes/beach-boat")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/landscapes/girl-urban-view", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/architecture-signs"))
            .description("Wake up to the sound of waves at this stunning beachside boutique hotel on Negombo's golden coast — just 10 minutes from the international airport.")
            .hostName("Ravi Gunawardena").hostBio("Superhost · 8 years experience").hostYears(8).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("7000")).averageRating(4.98).reviewCount(211).published(true)
            .amenities("Wifi:Free WiFi,Waves:Beachfront Pool,Wind:Air Conditioning,Utensils:Seafood Restaurant,Bike:Bicycle Rental,ShieldCheck:24hr Security,Dumbbell:Gym,Car:Airport Shuttle")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Beachfront Deluxe Room").roomType("DOUBLE").maxOccupancy(2).sqft(450).bedType("1 King Bed").pricePerNight(new BigDecimal("95000")).originalPrice(new BigDecimal("115000")).tag("Last rooms").features("Direct beach access,Outdoor shower").imageSrc(IMG+"/v1/samples/landscapes/beach-boat").available(true).build(),
            Room.builder().property(p).name("Garden Pool Villa").roomType("SUITE").maxOccupancy(4).sqft(750).bedType("2 Queen Beds").pricePerNight(new BigDecimal("150000")).tag("Popular").features("Private plunge pool,Outdoor dining").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build()
        ));
    }

    private void seedProperty6() {
        Property p = Property.builder()
            .name("Ella Mountain Eco Cabin").city("Ella").address("Ella Gap Road, Ella, Sri Lanka")
            .propertyType("Villa").latitude(6.8728).longitude(81.0466)
            .imageSrc(IMG + "/v1/samples/landscapes/nature-mountains")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/girl-urban-view", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/architecture-signs"))
            .description("Nestled within a working tea estate high above the famous Ella Gap, this sustainably-built eco cabin offers complete immersion in Sri Lanka's central highlands.")
            .hostName("Nimal Rathnayake").hostBio("Eco host · 4 years experience").hostYears(4).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("3000")).averageRating(4.88).reviewCount(134).published(true)
            .amenities("Wifi:Solar WiFi,Leaf:Eco/Solar Power,Coffee:Tea Plantation Tour,Utensils:Farm-to-table Meals,Bike:Hiking Trails,ShieldCheck:Night Security")
            .build();
        p = propertyRepository.save(p);
        roomRepository.save(Room.builder().property(p).name("Tea Estate Cabin").roomType("DOUBLE").maxOccupancy(2).sqft(280).bedType("1 Queen Bed").pricePerNight(new BigDecimal("45000")).originalPrice(new BigDecimal("55000")).tag("Refundable").features("Mountain-view deck,Outdoor shower,Hammock").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build());
    }

    private void seedProperty7() {
        Property p = Property.builder()
            .name("Mirissa Oceanfront Villa").city("Mirissa").address("Mirissa Bay Road, Mirissa, Sri Lanka")
            .propertyType("Villa").badge("Guest favorite").latitude(5.9488).longitude(80.4593)
            .imageSrc(IMG + "/v1/samples/landscapes/beach-boat")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/landscapes/architecture-signs", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/girl-urban-view"))
            .description("Sri Lanka's most iconic oceanfront villa — positioned directly on Mirissa's crescent bay with uninterrupted 180° views of the Indian Ocean.")
            .hostName("Saman Wickramasinghe").hostBio("Superhost · 6 years experience").hostYears(6).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("10000")).averageRating(4.96).reviewCount(88).published(true)
            .amenities("Wifi:Free WiFi,Waves:Ocean-edge Infinity Pool,Wind:Air Conditioning,Utensils:Private Chef,Car:Airport Transfer,ShieldCheck:24hr Security,Dumbbell:Yoga Deck,Coffee:Butler on call")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Ocean Master Suite").roomType("SUITE").maxOccupancy(2).sqft(650).bedType("1 King Bed").pricePerNight(new BigDecimal("120000")).originalPrice(new BigDecimal("145000")).tag("Popular").features("Sea-facing terrace,Rain shower,Soaking tub").imageSrc(IMG+"/v1/samples/landscapes/beach-boat").available(true).build(),
            Room.builder().property(p).name("Coral Bay Villa").roomType("SUITE").maxOccupancy(6).sqft(1200).bedType("3 King Beds").pricePerNight(new BigDecimal("250000")).features("Entire lower villa,Private beach access,Chef included").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build()
        ));
    }

    private void seedProperty8() {
        Property p = Property.builder()
            .name("Galle Dutch Period Mansion").city("Galle Fort").address("22 Leyn Baan Street, Galle Fort, Sri Lanka")
            .propertyType("Villa").badge("Superhost").latitude(6.0309).longitude(80.2157)
            .imageSrc(IMG + "/v1/samples/food/spices")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/landscapes/girl-urban-view", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/landscapes/architecture-signs"))
            .description("A masterwork of 18th-century Dutch colonial architecture — fully restored with museum-quality antiques and original Portuguese tile floors.")
            .hostName("Anjalee Perera").hostBio("Superhost · 9 years experience").hostYears(9).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("12000")).averageRating(4.91).reviewCount(45).published(true)
            .amenities("Wifi:Free WiFi,Waves:Courtyard Pool,Wind:Air Conditioning,Utensils:Heritage Restaurant,BookOpen:Antique Library,ShieldCheck:24hr Security,Coffee:Butler Service,Car:Chauffeur")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Dutch Governor's Suite").roomType("SUITE").maxOccupancy(2).sqft(800).bedType("1 King Canopy Bed").pricePerNight(new BigDecimal("180000")).originalPrice(new BigDecimal("210000")).tag("Last rooms").features("Courtyard views,Original tile floors,Clawfoot bathtub").imageSrc(IMG+"/v1/samples/food/spices").available(true).build(),
            Room.builder().property(p).name("Rampart View Room").roomType("DOUBLE").maxOccupancy(2).sqft(420).bedType("1 Queen Bed").pricePerNight(new BigDecimal("120000")).tag("Refundable").features("Fort wall views,Antique writing desk").imageSrc(IMG+"/v1/samples/landscapes/architecture-signs").available(true).build()
        ));
    }

    private void seedProperty9() {
        Property p = Property.builder()
            .name("Nuwara Eliya Tea Planter's Bungalow").city("Nuwara Eliya").address("St Andrew's Drive, Nuwara Eliya, Sri Lanka")
            .propertyType("Guesthouse").latitude(6.9497).longitude(80.7891)
            .imageSrc(IMG + "/v1/samples/landscapes/girl-urban-view")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/architecture-signs"))
            .description("A colonial-era tea planter's bungalow set amid 400 acres of pristine tea gardens at 6,000 feet. Log fires, tartan armchairs, and silver tea service await.")
            .hostName("Victor Steuart").hostBio("Heritage host · 11 years experience").hostYears(11).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("5000")).averageRating(4.82).reviewCount(109).published(true)
            .amenities("Wifi:Free WiFi,Coffee:Silver Tea Service,Wind:Fireplace,Utensils:Colonial Dining,Bike:Estate Walks,ShieldCheck:Night Security")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Planter's Master Suite").roomType("SUITE").maxOccupancy(2).sqft(550).bedType("1 King Bed").pricePerNight(new BigDecimal("65000")).originalPrice(new BigDecimal("78000")).tag("Popular").features("Fireplace,Mountain views,Claw-foot tub").imageSrc(IMG+"/v1/samples/landscapes/girl-urban-view").available(true).build(),
            Room.builder().property(p).name("Tea Garden Room").roomType("DOUBLE").maxOccupancy(2).sqft(350).bedType("1 Queen Bed").pricePerNight(new BigDecimal("48000")).tag("Refundable").features("Garden views,Writing desk").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build()
        ));
    }

    private void seedProperty10() {
        Property p = Property.builder()
            .name("Trincomalee Bay Resort").city("Trincomalee").address("Uppuveli Beach Road, Trincomalee, Sri Lanka")
            .propertyType("Hotel").badge("Guest favorite").latitude(8.5874).longitude(81.2152)
            .imageSrc(IMG + "/v1/samples/landscapes/beach-boat")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/architecture-signs", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/girl-urban-view"))
            .description("A tropical beachfront resort on Trincomalee's turquoise Uppuveli Beach. Diving, snorkeling, and whale watching at your doorstep.")
            .hostName("Kasun Rajapaksa").hostBio("Resort host · 5 years experience").hostYears(5).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("6000")).averageRating(4.89).reviewCount(156).published(true)
            .amenities("Wifi:Free WiFi,Waves:Beachfront Pool,Wind:Air Conditioning,Utensils:Open-air Restaurant,Dumbbell:Dive Center,ShieldCheck:24hr Security,Car:Airport Transfer,Coffee:Beach Bar")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Beach Bungalow").roomType("DOUBLE").maxOccupancy(2).sqft(400).bedType("1 King Bed").pricePerNight(new BigDecimal("72000")).originalPrice(new BigDecimal("85000")).tag("Popular").features("Direct beach access,Hammock,Outdoor shower").imageSrc(IMG+"/v1/samples/landscapes/beach-boat").available(true).build(),
            Room.builder().property(p).name("Ocean Suite").roomType("SUITE").maxOccupancy(4).sqft(680).bedType("2 Queen Beds").pricePerNight(new BigDecimal("110000")).features("Panoramic ocean views,Private balcony,Jacuzzi").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build()
        ));
    }

    private void seedProperty11() {
        Property p = Property.builder()
            .name("Sigiriya Jungle Lodge").city("Sigiriya").address("Sigiriya Road, Dambulla, Sri Lanka")
            .propertyType("Villa").latitude(7.9570).longitude(80.7603)
            .imageSrc(IMG + "/v1/samples/landscapes/nature-mountains")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/girl-urban-view", IMG+"/v1/samples/landscapes/architecture-signs"))
            .description("A luxury jungle lodge at the base of the iconic Sigiriya Rock Fortress. Wake to the calls of exotic birds and elephants passing in the distance.")
            .hostName("Lakmal Bandara").hostBio("Nature host · 3 years experience").hostYears(3).hostSuperhost(false)
            .baseGuests(2).extraGuestFee(new BigDecimal("4500")).averageRating(4.78).reviewCount(87).published(true)
            .amenities("Wifi:Free WiFi,Waves:Plunge Pool,Utensils:Bush Dining,Bike:Safari Tours,ShieldCheck:Night Security,Coffee:Nature Bar")
            .build();
        p = propertyRepository.save(p);
        roomRepository.save(Room.builder().property(p).name("Jungle Pavilion").roomType("SUITE").maxOccupancy(2).sqft(500).bedType("1 King Bed").pricePerNight(new BigDecimal("58000")).originalPrice(new BigDecimal("68000")).tag("Refundable").features("Open-air bathroom,Rock fortress views,Mosquito net canopy").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build());
    }

    private void seedProperty12() {
        Property p = Property.builder()
            .name("Bentota River House").city("Bentota").address("River Avenue, Bentota, Sri Lanka")
            .propertyType("Guesthouse").badge("Superhost").latitude(6.4271).longitude(79.9977)
            .imageSrc(IMG + "/v1/samples/landscapes/architecture-signs")
            .galleryImages(String.join(",", IMG+"/v1/samples/landscapes/beach-boat", IMG+"/v1/samples/landscapes/nature-mountains", IMG+"/v1/samples/food/spices", IMG+"/v1/samples/landscapes/girl-urban-view"))
            .description("A charming river house where the Bentota River meets the Indian Ocean. Kayak, fish, and birdwatch from your private jetty.")
            .hostName("Malini Jayasuriya").hostBio("Superhost · 6 years experience").hostYears(6).hostSuperhost(true)
            .baseGuests(2).extraGuestFee(new BigDecimal("4000")).averageRating(4.94).reviewCount(72).published(true)
            .amenities("Wifi:Free WiFi,Waves:River Pool,Wind:Air Conditioning,Utensils:Home Cooking,Bike:Kayak Rental,ShieldCheck:Night Security,Car:Beach Shuttle")
            .build();
        p = propertyRepository.save(p);
        roomRepository.saveAll(List.of(
            Room.builder().property(p).name("Riverfront Suite").roomType("SUITE").maxOccupancy(2).sqft(420).bedType("1 King Bed").pricePerNight(new BigDecimal("42000")).originalPrice(new BigDecimal("50000")).tag("Popular").features("River views,Private balcony,Writing desk").imageSrc(IMG+"/v1/samples/landscapes/architecture-signs").available(true).build(),
            Room.builder().property(p).name("Garden Room").roomType("DOUBLE").maxOccupancy(2).sqft(300).bedType("1 Queen Bed").pricePerNight(new BigDecimal("32000")).tag("Refundable").features("Garden access,Hammock").imageSrc(IMG+"/v1/samples/landscapes/nature-mountains").available(true).build()
        ));
    }
}
