package com.b4code.backend.dao;

import com.b4code.backend.models.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("""
            SELECT DISTINCT p FROM Property p
            JOIN RoomType r ON r.property = p
            WHERE (LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.country) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%')))


              AND r.pricePerNight >= :minPrice
              AND r.pricePerNight <= :maxPrice

              AND (
                  SELECT COALESCE(SUM(
                      r2.inventory - CASE WHEN :hasDates = true THEN (
                          SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                          WHERE ri.roomType = r2
                              AND ri.date >= :checkIn
                              AND ri.date < :checkOut
                      ) ELSE 0 END
                  ), 0) FROM RoomType r2 WHERE r2.property = p
                  AND r2.inventory > CASE WHEN :hasDates = true THEN (
                      SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                      WHERE ri.roomType = r2
                          AND ri.date >= :checkIn
                          AND ri.date < :checkOut
                  ) ELSE 0 END
              ) >= :roomTypes
              AND (
                  SELECT COALESCE(SUM(
                      r3.maxOccupancy * (
                          r3.inventory - CASE WHEN :hasDates = true THEN (
                              SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                              WHERE ri.roomType = r3
                                  AND ri.date >= :checkIn
                                  AND ri.date < :checkOut
                          ) ELSE 0 END
                      )
                  ), 0) FROM RoomType r3 WHERE r3.property = p
                  AND r3.inventory > CASE WHEN :hasDates = true THEN (
                      SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                      WHERE ri.roomType = r3
                          AND ri.date >= :checkIn
                          AND ri.date < :checkOut
                  ) ELSE 0 END
              ) >= :guests
            """)
    Page<Property> searchAvailableProperties(
            @Param("destination") String destination,
            @Param("hasDates") boolean hasDates,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") Integer guests,
            @Param("roomTypes") Integer roomTypes,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Property p
            JOIN RoomType r ON r.property = p
            WHERE (LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.country) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%')))


              AND r.pricePerNight >= :minPrice
              AND r.pricePerNight <= :maxPrice
              AND (
                  SELECT COALESCE(SUM(
                      r2.inventory - CASE WHEN :hasDates = true THEN (
                          SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                          WHERE ri.roomType = r2
                              AND ri.date >= :checkIn
                              AND ri.date < :checkOut
                      ) ELSE 0 END
                  ), 0) FROM RoomType r2 WHERE r2.property = p
                  AND r2.inventory > CASE WHEN :hasDates = true THEN (
                      SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                      WHERE ri.roomType = r2
                          AND ri.date >= :checkIn
                          AND ri.date < :checkOut
                  ) ELSE 0 END
              ) >= :roomTypes
              AND (
                  SELECT COALESCE(SUM(
                      r3.maxOccupancy * (
                          r3.inventory - CASE WHEN :hasDates = true THEN (
                              SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                              WHERE ri.roomType = r3
                                  AND ri.date >= :checkIn
                                  AND ri.date < :checkOut
                          ) ELSE 0 END
                      )
                  ), 0) FROM RoomType r3 WHERE r3.property = p
                  AND r3.inventory > CASE WHEN :hasDates = true THEN (
                      SELECT COALESCE(MAX(ri.bookedQuantity), 0) FROM RoomDateInventory ri
                      WHERE ri.roomType = r3
                          AND ri.date >= :checkIn
                          AND ri.date < :checkOut
                  ) ELSE 0 END
              ) >= :guests
            """)
    List<Property> searchAvailablePropertiesList(
            @Param("destination") String destination,
            @Param("hasDates") boolean hasDates,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") Integer guests,
            @Param("roomTypes") Integer roomTypes,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT DISTINCT p.city FROM Property p ORDER BY p.city")
    List<String> findDistinctCities();

    // 🛠️🛠️🛠️ Admin Management Methods (from admin module)
    // 🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️

    /**
     * Admin query to list ALL properties with optional search filters.
     * Includes both PV-* (verification queue) and PROP-* (guest-seeded) properties.
     */
    @Query("""
            SELECT p FROM Property p
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(p.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.city)      LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<Property> findAllWithFilters(@Param("search") String search, @Param("status") com.b4code.backend.models.enums.PropertyStatus status, Pageable pageable);

    List<Property> findTop5ByOrderByIdDesc();

    List<Property> findByOwnerId(Long ownerId);

    @Query("""
            SELECT p FROM Property p
            WHERE p.ownerId = :ownerId
              AND (:search IS NULL OR :search = ''
                   OR LOWER(p.name)         LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.city)         LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.addressLine1) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR p.status = :status)
            ORDER BY p.id DESC
            """)
    Page<Property> findByOwnerWithFilters(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("status") com.b4code.backend.models.enums.PropertyStatus status,
            Pageable pageable);

    @Query("SELECT MIN(r.pricePerNight) FROM RoomType r")
    BigDecimal findMinPrice();

    @Query("SELECT MAX(r.pricePerNight) FROM RoomType r")
    BigDecimal findMaxPrice();

    long countByCreatedAtAfter(LocalDateTime date);
}
