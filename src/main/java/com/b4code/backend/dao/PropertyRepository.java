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
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("""
            SELECT DISTINCT p FROM Property p
            JOIN Room r ON r.property = p
            WHERE (LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.country) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%')))
              
              
              AND r.pricePerNight >= :minPrice
              AND r.pricePerNight <= :maxPrice
              
              AND (
                    :checkIn IS NULL OR :checkOut IS NULL OR NOT EXISTS (
                        SELECT b FROM Booking b
                        WHERE b.room = r
                            AND b.checkIn  < :checkOut
                            AND b.checkOut > :checkIn
                    )
              )
              AND (
                  SELECT COUNT(r2) FROM Room r2 WHERE r2.property = p
                  
                  
                  AND (
                      :checkIn IS NULL OR :checkOut IS NULL OR NOT EXISTS (
                          SELECT b FROM Booking b
                          WHERE b.room = r2
                              AND b.checkIn  < :checkOut
                              AND b.checkOut > :checkIn
                      )
                  )
              ) >= :rooms
            """)
    Page<Property> searchAvailableProperties(
            @Param("destination") String destination,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("rooms") Integer rooms,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Property p
            JOIN Room r ON r.property = p
            WHERE (LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.country) LIKE LOWER(CONCAT('%', :destination, '%'))
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%')))
              
              
              AND r.pricePerNight >= :minPrice
              AND r.pricePerNight <= :maxPrice
              AND (
                    :checkIn IS NULL OR :checkOut IS NULL OR NOT EXISTS (
                        SELECT b FROM Booking b
                        WHERE b.room = r
                            AND b.checkIn  < :checkOut
                            AND b.checkOut > :checkIn
                    )
              )
              AND (
                  SELECT COUNT(r2) FROM Room r2 WHERE r2.property = p
                  
                  
                  AND (
                      :checkIn IS NULL OR :checkOut IS NULL OR NOT EXISTS (
                          SELECT b FROM Booking b
                          WHERE b.room = r2
                              AND b.checkIn  < :checkOut
                              AND b.checkOut > :checkIn
                      )
                  )
              ) >= :rooms
            """)
    List<Property> searchAvailablePropertiesList(
            @Param("destination") String destination,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("rooms") Integer rooms,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    

    @Query("SELECT DISTINCT p.city FROM Property p ORDER BY p.city")
    List<String> findDistinctCities();

    // 🛠️🛠️🛠️ Admin Management Methods (from admin module) 🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️🛠️

    /**
     * Admin query to list ALL properties with optional search filters.
     * Includes both PV-* (verification queue) and PROP-* (guest-seeded) properties.
     */
    @Query("""
            SELECT p FROM Property p
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(p.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.city)      LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Property> findAllWithFilters(@Param("search") String search, Pageable pageable);

    List<Property> findTop5ByOrderByIdDesc();

    List<Property> findByOwnerId(Long ownerId);

    

    @Query("SELECT MIN(r.pricePerNight) FROM Room r")
    BigDecimal findMinPrice();

    @Query("SELECT MAX(r.pricePerNight) FROM Room r")
    BigDecimal findMaxPrice();
}
