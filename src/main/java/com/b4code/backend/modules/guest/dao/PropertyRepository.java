package com.b4code.backend.modules.guest.dao;

import com.b4code.backend.modules.guest.models.Booking;
import com.b4code.backend.modules.guest.models.Property;
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
     * Search published properties that have at least one room available
     * (not overlapping with any confirmed booking) within the price range.
     */
    @Query("""
        SELECT DISTINCT p FROM Property p
        JOIN p.rooms r
        WHERE p.published = true
          AND (:destination IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :destination, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :destination, '%')))
          AND r.maxOccupancy >= :guests
          AND r.available = true
          AND (:minPrice IS NULL OR r.pricePerNight >= :minPrice)
          AND (:maxPrice IS NULL OR r.pricePerNight <= :maxPrice)
          AND (:minRating IS NULL OR p.averageRating >= :minRating)
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
}