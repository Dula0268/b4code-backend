package com.b4code.backend.modules.guest.dao;

import com.b4code.backend.models.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository("guestPropertyRepository")
public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * Paginated search for published properties with optional filters.
     * Filters by destination, guests, price range, rating, property types, and date availability.
     */
    @Query("""
        SELECT DISTINCT p FROM GuestProperty p
        JOIN Room r ON r.property = p
        WHERE p.published = true
          AND (LOWER(p.destination) LIKE LOWER(CONCAT('%', :destination, '%'))
               OR LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%'))
               OR LOWER(p.address) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND r.maxOccupancy >= :guests
          AND r.available = true
          AND r.pricePerNight >= :minPrice
          AND r.pricePerNight <= :maxPrice
          AND p.averageRating >= :minRating
          AND p.propertyType IN :propertyTypes
          AND (
                :checkIn IS NULL OR :checkOut IS NULL OR NOT EXISTS (
                    SELECT b FROM Booking b
                    WHERE b.room = r
                        AND b.status NOT IN ('CANCELLED')
                        AND b.checkIn  < :checkOut
                        AND b.checkOut > :checkIn
                )
          )
    """)
    Page<Property> searchAvailableProperties(
        @Param("destination") String destination,
        @Param("checkIn")     LocalDate checkIn,
        @Param("checkOut")    LocalDate checkOut,
        @Param("guests")      Integer guests,
        @Param("minPrice")    BigDecimal minPrice,
        @Param("maxPrice")    BigDecimal maxPrice,
        @Param("minRating")   Double minRating,
        @Param("propertyTypes") List<String> propertyTypes,
        Pageable pageable
    );

    /**
     * Non-paginated version used internally (e.g., for property detail).
     */
    @Query("""
        SELECT DISTINCT p FROM GuestProperty p
        JOIN Room r ON r.property = p
        WHERE p.published = true
          AND (LOWER(p.destination) LIKE LOWER(CONCAT('%', :destination, '%'))
               OR LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND r.maxOccupancy >= :guests
          AND r.available = true
          AND r.pricePerNight >= :minPrice
          AND r.pricePerNight <= :maxPrice
          AND p.averageRating >= :minRating
          AND (
                :checkIn IS NULL OR :checkOut IS NULL OR NOT EXISTS (
                    SELECT b FROM Booking b
                    WHERE b.room = r
                        AND b.status NOT IN ('CANCELLED')
                        AND b.checkIn  < :checkOut
                        AND b.checkOut > :checkIn
                )
          )
    """)
    List<Property> searchAvailableProperties(
        @Param("destination") String destination,
        @Param("checkIn")     LocalDate checkIn,
        @Param("checkOut")    LocalDate checkOut,
        @Param("guests")      Integer guests,
        @Param("minPrice")    BigDecimal minPrice,
        @Param("maxPrice")    BigDecimal maxPrice,
        @Param("minRating")   Double minRating
    );

    // ─── Aggregate queries for dynamic filter options ────────────────────

    @Query("SELECT DISTINCT p.propertyType FROM GuestProperty p WHERE p.published = true ORDER BY p.propertyType")
    List<String> findDistinctPropertyTypes();

    @Query("SELECT DISTINCT p.city FROM GuestProperty p WHERE p.published = true ORDER BY p.city")
    List<String> findDistinctCities();

    @Query("SELECT MIN(r.pricePerNight) FROM Room r WHERE r.property.published = true AND r.available = true")
    BigDecimal findMinPrice();

    @Query("SELECT MAX(r.pricePerNight) FROM Room r WHERE r.property.published = true AND r.available = true")
    BigDecimal findMaxPrice();

    @Query("SELECT p.propertyType, COUNT(p) FROM GuestProperty p WHERE p.published = true GROUP BY p.propertyType")
    List<Object[]> countByPropertyType();

    long countByPublishedTrue();
}
