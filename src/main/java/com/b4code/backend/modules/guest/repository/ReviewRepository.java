package com.b4code.backend.modules.guest.repository;

import com.b4code.backend.modules.guest.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPropertyId(Long propertyId);
    List<Review> findByGuestId(Long guestId);
    List<Review> findByPropertyIdAndGuestId(Long propertyId, Long guestId);
}
