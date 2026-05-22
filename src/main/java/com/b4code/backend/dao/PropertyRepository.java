package com.b4code.backend.dao;

import com.b4code.backend.models.Property;
import com.b4code.backend.models.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // ─── Guest Search Methods (from guest module) ────────────────────

    /**
     * Paginated search for published properties with optional filters.
     * Filters by destination, guests, price range, rating, property types, and date availability.
     */
    @Query("""
        SELECT DISTINCT p FROM Property p
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
        SELECT DISTINCT p FROM Property p
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
    List<Property> searchAvailablePropertiesList(
        @Param("destination") String destination,
        @Param("checkIn")     LocalDate checkIn,
        @Param("checkOut")    LocalDate checkOut,
        @Param("guests")      Integer guests,
        @Param("minPrice")    BigDecimal minPrice,
        @Param("maxPrice")    BigDecimal maxPrice,
        @Param("minRating")   Double minRating
    );

    @Query("SELECT DISTINCT p.propertyType FROM Property p WHERE p.published = true ORDER BY p.propertyType")
    List<String> findDistinctPropertyTypes();

    @Query("SELECT DISTINCT p.city FROM Property p WHERE p.published = true ORDER BY p.city")
    List<String> findDistinctCities();

    // ─── Admin Management Methods (from admin module) ────────────────────

    /**
     * Admin query to filter properties with verification (PV-*) by status and search.
     */
    @Query("""
            SELECT p FROM Property p
            WHERE p.pvId LIKE 'PV-%'
              AND (:status IS NULL OR p.status = :status)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(p.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.pvId)      LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.ownerName) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Property> findAllWithFilters(
            @Param("status") PropertyStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT p FROM Property p WHERE p.pvId LIKE 'PV-%' AND p.status IN :statuses ORDER BY p.submittedAt DESC LIMIT 5")
    List<Property> findTop5ByStatusInOrderBySubmittedAtDesc(@Param("statuses") List<PropertyStatus> statuses);

    List<Property> findByStatus(PropertyStatus status);

    List<Property> findByOwnerId(Long ownerId);

    long countByPvIdIsNotNull();

    long countByPvIdStartingWith(String prefix);
}
