package com.b4code.backend.dao;

import com.b4code.backend.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByPropertyIdOrderByIdDesc(Long propertyId, Pageable pageable);

    List<Review> findByPropertyIdOrderByIdDesc(Long propertyId);

    Optional<Review> findByBookingId(Long bookingId);

    @Query("""
        SELECT AVG(r.overallRating) FROM Review r
        WHERE r.property.id = :propertyId
    """)
    Double calculateAverageRating(@Param("propertyId") Long propertyId);

    Long countByPropertyId(Long propertyId);

    boolean existsByBookingId(Long bookingId);
}
