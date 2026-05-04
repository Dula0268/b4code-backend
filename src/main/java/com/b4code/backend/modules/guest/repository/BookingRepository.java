package com.b4code.backend.modules.guest.repository;

import com.b4code.backend.modules.guest.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestId(Long guestId);
    List<Booking> findByPropertyId(Long propertyId);
    List<Booking> findByGuestIdAndPropertyId(Long guestId, Long propertyId);
}
